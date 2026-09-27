package net.modpackmuseum.core.machine;

import net.modpackmuseum.core.energy.EnergyBuffer;

/** Pure generator state machine: committed fuel burn-time is converted into bounded energy. */
public final class GeneratorRuntime {
    private final EnergyBuffer energy;
    private final long energyPerTick;
    private long burnTicksRemaining;

    public GeneratorRuntime(long energyCapacity, long energyPerTick) {
        if (energyPerTick <= 0) {
            throw new IllegalArgumentException("energyPerTick must be > 0");
        }
        this.energy = new EnergyBuffer(energyCapacity);
        this.energyPerTick = energyPerTick;
    }

    public EnergyBuffer energy() {
        return energy;
    }

    public long burnTicksRemaining() {
        return burnTicksRemaining;
    }

    public GeneratorSnapshot snapshot() {
        return new GeneratorSnapshot(
            GeneratorSnapshot.CURRENT_SCHEMA,
            energy.stored(),
            burnTicksRemaining
        );
    }

    public static GeneratorRuntime restore(long energyCapacity, long energyPerTick, GeneratorSnapshot snapshot) {
        GeneratorRuntime runtime = new GeneratorRuntime(energyCapacity, energyPerTick);
        runtime.restoreState(snapshot);
        return runtime;
    }

    /** Restores committed state while preserving the identity of the backing energy buffer. */
    public void restoreState(GeneratorSnapshot snapshot) {
        if (snapshot == null) {
            throw new NullPointerException("snapshot");
        }
        if (snapshot.energyStored() > energy.capacity()) {
            throw new IllegalArgumentException("persisted generator energy exceeds capacity");
        }
        energy.extract(energy.stored());
        energy.receive(snapshot.energyStored());
        burnTicksRemaining = snapshot.burnTicksRemaining();
    }

    public void addBurnTicks(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must be >= 0");
        }
        burnTicksRemaining = Math.addExact(burnTicksRemaining, ticks);
    }

    /**
     * Converts one burn tick to energy. Fuel pauses while the local buffer is full, avoiding
     * silent loss in the first implementation. Historical always-burning semantics can be layered later.
     */
    public long tick() {
        if (burnTicksRemaining == 0 || energy.space() == 0) {
            return 0;
        }
        long generated = energy.receive(energyPerTick);
        if (generated > 0) {
            burnTicksRemaining--;
        }
        return generated;
    }
}
