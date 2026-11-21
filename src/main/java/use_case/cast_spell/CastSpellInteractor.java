package use_case.cast_spell;

import entity.Player;
import entity.Projectile;
import entity.Spell;
import java.util.Map;
import java.util.Objects;

/**
 * Validates casting requests and launches projectiles from the player's centre.
 */
public final class CastSpellInteractor implements CastSpellInputBoundary {
    private final CastSpellDataAccessInterface dataAccess;
    private final CastSpellOutputBoundary presenter;
    private final Map<String, Spell> spells;
    private final double projectileSpeed;
    private final double projectileWidth;
    private final double projectileHeight;

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
        this.spells = Map.copyOf(spells);

        if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0
                || !Double.isFinite(projectileWidth) || projectileWidth <= 0
                || !Double.isFinite(projectileHeight) || projectileHeight <= 0) {
            throw new IllegalArgumentException(
                    "Projectile speed and dimensions must be positive and finite");
        }

        this.projectileSpeed = projectileSpeed;
        this.projectileWidth = projectileWidth;
        this.projectileHeight = projectileHeight;
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

        Spell spell = spells.get(spellId);
        Projectile projectile = new Projectile(
                spell,
                centreX - projectileWidth / 2.0,
                centreY - projectileHeight / 2.0,
                directionX / distance * projectileSpeed,
                directionY / distance * projectileSpeed,
                projectileWidth,
                projectileHeight);

        dataAccess.addProjectile(projectile);
        presenter.prepareSuccessView(
                new CastSpellOutputData(spell.getId(), spell.getName()));
    }
}