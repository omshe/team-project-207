package interface_adapter.cast_spell;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import use_case.cast_spell.CastSpellInputBoundary;
import use_case.cast_spell.CastSpellInputData;
import use_case.cast_spell.UpdateProjectilesInputBoundary;

/**
 * Queues casting input and forwards it to the use cases on the game-loop thread.
 * Spell validation and projectile rules belong to the interactors.
 */
public final class CastSpellController {
    private final CastSpellInputBoundary castSpell;
    private final UpdateProjectilesInputBoundary updateProjectiles;
    private final Queue<CastSpellInputData> pendingRequests =
            new ConcurrentLinkedQueue<>();

    /**
     * Creates a controller for launching and updating projectiles.
     *
     * @param castSpell the launch use case
     * @param updateProjectiles the projectile simulation use case
     * @throws NullPointerException if either boundary is null
     */
    public CastSpellController(CastSpellInputBoundary castSpell,
                               UpdateProjectilesInputBoundary updateProjectiles) {
        this.castSpell = Objects.requireNonNull(castSpell);
        this.updateProjectiles = Objects.requireNonNull(updateProjectiles);
    }

    /**
     * Queues one immutable request without running either use case.
     * May be called from Swing's event thread or a recognition worker.
     * Values are forwarded unchanged for validation by the launch use case.
     *
     * @param spellId the requested spell identifier
     * @param targetWorldX the horizontal world reference used to establish direction
     * @param targetWorldY the vertical world reference used to establish direction
     */
    public void queueCast(String spellId, double targetWorldX, double targetWorldY) {
        pendingRequests.add(new CastSpellInputData(
                spellId, targetWorldX, targetWorldY));
    }

    /**
     * Forwards queued requests in queue order, then updates projectiles once.
     * Call once per tick on the game-loop thread, after player and enemy movement.
     * Newly launched projectiles participate in this tick's projectile update.
     * A request arriving during this method may be processed this tick or the next.
     * Only the game-loop thread may call this method.
     */
    public void tick() {
        CastSpellInputData request = pendingRequests.poll();
        while (request != null) {
            castSpell.execute(request);
            request = pendingRequests.poll();
        }
        updateProjectiles.execute();
    }
}