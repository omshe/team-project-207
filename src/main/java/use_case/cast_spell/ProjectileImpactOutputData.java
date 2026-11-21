package use_case.cast_spell;

/**
 * An immutable event locating a projectile's centre at its first enemy contact.
 */
public final class ProjectileImpactOutputData {
    private final String spellId;
    private final double positionX;
    private final double positionY;

    /**
     * Creates an impact event for visual playback.
     *
     * @param spellId the spell whose projectile hit an enemy
     * @param positionX the projectile centre's horizontal world coordinate at contact
     * @param positionY the projectile centre's vertical world coordinate at contact
     */
    public ProjectileImpactOutputData(String spellId, double positionX, double positionY) {
        this.spellId = spellId;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    /** Returns the impacting spell's identifier. */
    public String getSpellId() {
        return spellId;
    }

    /** Returns the impact centre's horizontal world coordinate. */
    public double getX() {
        return positionX;
    }

    /** Returns the impact centre's vertical world coordinate. */
    public double getY() {
        return positionY;
    }
}