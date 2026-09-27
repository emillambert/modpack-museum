package net.modpackmuseum.core.energy;

import java.util.Objects;

/** A machine/storage endpoint attached to an {@link EnergyNetwork}. */
public final class EnergyPort {
    private final String id;
    private final EnergyBuffer buffer;
    private final long maxInputPerTick;
    private final long maxOutputPerTick;

    public EnergyPort(String id, EnergyBuffer buffer, long maxInputPerTick, long maxOutputPerTick) {
        this.id = Objects.requireNonNull(id, "id");
        this.buffer = Objects.requireNonNull(buffer, "buffer");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (maxInputPerTick < 0 || maxOutputPerTick < 0) {
            throw new IllegalArgumentException("transfer limits must be >= 0");
        }
        this.maxInputPerTick = maxInputPerTick;
        this.maxOutputPerTick = maxOutputPerTick;
    }

    public String id() {
        return id;
    }

    public EnergyBuffer buffer() {
        return buffer;
    }

    public long maxInputPerTick() {
        return maxInputPerTick;
    }

    public long maxOutputPerTick() {
        return maxOutputPerTick;
    }
}
