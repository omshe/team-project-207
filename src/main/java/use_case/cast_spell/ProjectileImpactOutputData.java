package use_case.cast_spell;

/**
 * An immutable event locating a projectile's centre at its first enemy contact.
 */
public final class ProjectileImpactOutputData {
    private final String spellId;
    private final double x;
    private final double y;

    /**
     * Creates an impact event for visual playback.
     *
     * @param spellId the spell whose projectile hit an enemy
     * @param x the projectile centre's horizontal world coordinate at contact
     * @param y the projectile centre's vertical world coordinate at contact
     */
    public ProjectileImpactOutputData(String spellId, double x, double y) {
        this.spellId = spellId;
        this.x = x;
        this.y = y;
    }

    /** @return the impacting spell's identifier */
    public String getSpellId() {
        return spellId;
    }

    /** @return the impact centre's horizontal world coordinate */
    public double getX() {
        return x;
    }

    /** @return the impact centre's vertical world coordinate */
    public double getY() {
        return y;
    }
}