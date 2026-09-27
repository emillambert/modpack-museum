package net.modpackmuseum.core.energy;

/** Conservation-safe transfer helper used by the future cable/network scheduler. */
public final class EnergyTransfer {
    private EnergyTransfer() {
    }

    /**
     * Moves at most {@code limit} units from source to destination and returns the amount moved.
     * No energy is extracted until destination capacity is known, preventing accidental loss.
     */
    public static long transfer(EnergyBuffer source, EnergyBuffer destination, long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }

        long movable = Math.min(limit, Math.min(source.stored(), destination.space()));
        if (movable == 0) {
            return 0;
        }

        long extracted = source.extract(movable);
        long accepted = destination.receive(extracted);
        if (accepted != extracted) {
            // This cannot occur with the single-threaded primitive contract. Fail loudly if future
            // changes violate that assumption instead of silently destroying energy.
            throw new IllegalStateException("energy transfer lost conservation");
        }
        return accepted;
    }
}
