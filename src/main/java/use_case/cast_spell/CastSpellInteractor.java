package use_case.cast_spell;

import entity.Player;
import entity.Projectile;
import entity.ProjectileDefinition;
import entity.Spell;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Validates casting requests and launches projectiles from the player's centre.
 */
public final class CastSpellInteractor implements CastSpellInputBoundary {
    private final CastSpellDataAccessInterface dataAccess;
    private final CastSpellOutputBoundary presenter;
    private final Map<String, ProjectileDefinition> spells;

    /**
     * Creates a launch use case with injected session access and spell definitions.
     *
     * @param dataAccess access to the shared player and projectile collection
     * @param presenter where launch results are sent
     * @param spells supported spell definitions indexed by their identifiers
     * @param projectileSpeed positive, finite speed in pixels per tick
     * @param projectileWidth positive, finite hitbox width in pixels
     * @param projectileHeight positive, finite hitbox height in pixels
     * @throws NullPointerException if a collaborator or a map entry is null
     * @throws IllegalArgumentException if speed or dimensions are not positive and finite
     */
    public CastSpellInteractor(CastSpellDataAccessInterface dataAccess,
                               CastSpellOutputBoundary presenter,
                               Map<String, Spell> spells,
                               double projectileSpeed,
                               double projectileWidth,
                               double projectileHeight) {
        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.presenter = Objects.requireNonNull(presenter);
        Map<String, Spell> identities = Map.copyOf(spells);

        if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0
                || !Double.isFinite(projectileWidth) || projectileWidth <= 0
                || !Double.isFinite(projectileHeight) || projectileHeight <= 0) {
            throw new IllegalArgumentException(
                    "Projectile speed and dimensions must be positive and finite");
        }

        Map<String, ProjectileDefinition> definitions = new HashMap<>();
        for (Map.Entry<String, Spell> entry : identities.entrySet()) {
            definitions.put(entry.getKey(), new ProjectileDefinition(entry.getValue(),
                    entry.getValue().getId(), projectileSpeed, projectileWidth, projectileHeight));
        }
        this.spells = Map.copyOf(definitions);
    }

    /**
     * Creates a caster with separate immutable flight definitions for each spell.
     *
     * @param dataAccess shared player and projectile storage
     * @param presenter success/failure output boundary
     * @param definitions initial projectile definitions indexed by spell ID
     */
    public CastSpellInteractor(CastSpellDataAccessInterface dataAccess,
                               CastSpellOutputBoundary presenter,
                               Map<String, ProjectileDefinition> definitions) {
        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.presenter = Objects.requireNonNull(presenter);
        this.spells = Map.copyOf(definitions);
        for (Map.Entry<String, ProjectileDefinition> entry : spells.entrySet()) {
            if (!entry.getKey().equals(entry.getValue().getSpell().getId())) {
                throw new IllegalArgumentException("Definition key must match its spell ID");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(CastSpellInputData inputData) {
        Objects.requireNonNull(inputData);

        String spellId = inputData.getSpellId();
        if (spellId == null || !spells.containsKey(spellId)) {
            presenter.prepareFailView("That spell is unavailable.");
            return;
        }

        if (!Double.isFinite(inputData.getTargetWorldX())
                || !Double.isFinite(inputData.getTargetWorldY())) {
            presenter.prepareFailView("Choose a finite aim point.");
            return;
        }

        Player player = dataAccess.getPlayer();
        double centreX = player.getX() + player.getSize() / 2.0;
        double centreY = player.getY() + player.getSize() / 2.0;

        double directionX = inputData.getTargetWorldX() - centreX;
        double directionY = inputData.getTargetWorldY() - centreY;
        double distance = Math.hypot(directionX, directionY);

        if (distance == 0 || !Double.isFinite(distance)) {
            presenter.prepareFailView("Cannot determine a direction for that aim point.");
            return;
        }

        ProjectileDefinition definition = spells.get(spellId);
        Spell spell = definition.getSpell();
        Projectile projectile = new Projectile(definition,
                centreX - definition.getWidth() / 2.0,
                centreY - definition.getHeight() / 2.0,
                directionX / distance * definition.getSpeed(),
                directionY / distance * definition.getSpeed());

        dataAccess.addProjectile(projectile);
        presenter.prepareSuccessView(
                new CastSpellOutputData(spell.getId(), spell.getName()));
    }
}

