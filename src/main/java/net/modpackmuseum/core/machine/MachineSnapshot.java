package net.modpackmuseum.core.machine;

/**
 * Versioned persistence DTO for the first single-recipe machine runtime.
 * The Minecraft adapter can map this to world data without coupling the core to NBT APIs.
 */
public record MachineSnapshot(
    int schemaVersion,
    long energyStored,
    int progressTicks,
    String inputItem,
    int inputCount,
    String outputItem,
    int outputCount
) {
    public static final int CURRENT_SCHEMA = 1;

    public MachineSnapshot {
        if (schemaVersion != CURRENT_SCHEMA) {
            throw new IllegalArgumentException("unsupported machine schema: " + schemaVersion);
        }
        if (energyStored < 0 || progressTicks < 0 || inputCount < 0 || outputCount < 0) {
            throw new IllegalArgumentException("persisted numeric fields must be >= 0");
        }
        if (inputCount == 0) {
            inputItem = null;
        } else if (inputItem == null || inputItem.isBlank()) {
            throw new IllegalArgumentException("non-empty input requires an item id");
        }
        if (outputCount == 0) {
            outputItem = null;
        } else if (outputItem == null || outputItem.isBlank()) {
            throw new IllegalArgumentException("non-empty output requires an item id");
        }
    }
}
