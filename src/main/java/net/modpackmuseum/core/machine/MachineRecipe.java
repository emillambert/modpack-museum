package net.modpackmuseum.core.machine;

import java.util.Objects;

/**
 * Pure, single-input/single-output processing recipe used by the first industrial machines.
 * Minecraft item stacks are deliberately adapted at the block-entity boundary.
 */
public record MachineRecipe(
    String id,
    String inputItem,
    int inputCount,
    String outputItem,
    int outputCount,
    int durationTicks,
    long energyPerTick
) {
    public MachineRecipe {
        id = requireId(id, "id");
        inputItem = requireId(inputItem, "inputItem");
        outputItem = requireId(outputItem, "outputItem");
        if (inputCount <= 0 || outputCount <= 0) {
            throw new IllegalArgumentException("item counts must be > 0");
        }
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("durationTicks must be > 0");
        }
        if (energyPerTick < 0) {
            throw new IllegalArgumentException("energyPerTick must be >= 0");
        }
    }

    public long totalEnergy() {
        return Math.multiplyExact((long) durationTicks, energyPerTick);
    }

    private static String requireId(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
