package net.modpackmuseum.core.machine;

/** Versioned persistence DTO for the first fuel-to-energy generator runtime. */
public record GeneratorSnapshot(
    int schemaVersion,
    long energyStored,
    long burnTicksRemaining
) {
    public static final int CURRENT_SCHEMA = 1;

    public GeneratorSnapshot {
        if (schemaVersion != CURRENT_SCHEMA) {
            throw new IllegalArgumentException("unsupported generator schema: " + schemaVersion);
        }
        if (energyStored < 0 || burnTicksRemaining < 0) {
            throw new IllegalArgumentException("persisted generator fields must be >= 0");
        }
    }
}
