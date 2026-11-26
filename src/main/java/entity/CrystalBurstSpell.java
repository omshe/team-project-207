package entity;

/** Defines the identity shared by the crystal core and all of its shards. */
public final class CrystalBurstSpell implements Spell {
    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "crystal_burst";
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Crystal Burst";
    }
}
