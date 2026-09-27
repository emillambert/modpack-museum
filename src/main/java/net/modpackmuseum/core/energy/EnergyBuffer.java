package net.modpackmuseum.core.energy;

/**
 * Small server-authoritative energy primitive for the upcoming Classic Tech slice.
 *
 * <p>It intentionally knows nothing about Minecraft. Keeping conservation logic pure makes it
 * easy to test before cable graphs, machines and persistence are attached to it.</p>
 */
public final class EnergyBuffer {
    private final long capacity;
    private long stored;

    public EnergyBuffer(long capacity) {
        this(capacity, 0);
    }

    public EnergyBuffer(long capacity, long initialStored) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        if (initialStored < 0 || initialStored > capacity) {
            throw new IllegalArgumentException("initialStored must be in [0, capacity]");
        }
        this.capacity = capacity;
        this.stored = initialStored;
    }

    public long capacity() {
        return capacity;
    }

    public long stored() {
        return stored;
    }

    public long space() {
        return capacity - stored;
    }

    /** Returns the amount actually accepted. */
    public long receive(long offered) {
        requireNonNegative(offered, "offered");
        long accepted = Math.min(offered, space());
        stored += accepted;
        return accepted;
    }

    /** Returns the amount actually extracted. */
    public long extract(long requested) {
        requireNonNegative(requested, "requested");
        long extracted = Math.min(requested, stored);
        stored -= extracted;
        return extracted;
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
    }
}
