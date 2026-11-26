package interface_adapter.cast_spell;

import java.util.Objects;

/** Immutable location of one impact animation to start when the view receives it. */
public final class ProjectileImpactDisplayData {
    private final String spellId;
    private final String projectileTypeId;
    private final double centerX;
    private final double centerY;

    /**
     * Creates one visual event. Separate instances represent separate impacts,
     * even when their spell identifiers and positions match.
     *
     * @param spellId the impacting spell's identifier
     * @param centerX horizontal impact center in world pixels
     * @param centerY vertical impact center in world pixels
     */
    public ProjectileImpactDisplayData(String spellId, double centerX, double centerY) {
        this(spellId, spellId, centerX, centerY);
    }

    /**
     * Creates values with separate spell identity and domain projectile type.
     *
     * @param spellId originating spell
     * @param projectileTypeId domain projectile type
     * @param centerX impact centre X
     * @param centerY impact centre Y
     */
    public ProjectileImpactDisplayData(String spellId, String projectileTypeId,
                                       double centerX, double centerY) {
        this.spellId = Objects.requireNonNull(spellId);
        this.projectileTypeId = Objects.requireNonNull(projectileTypeId);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /** Returns the domain projectile type used for renderer registration. */
    public String getProjectileTypeId() {
        return projectileTypeId;
    }

    /** Returns the impacting spell's identifier. */
    public String getSpellId() {
        return spellId;
    }

    /** Returns the horizontal impact center in world pixels. */
    public double getCenterX() {
        return centerX;
    }

    /** Returns the vertical impact center in world pixels. */
    public double getCenterY() {
        return centerY;
    }
}
