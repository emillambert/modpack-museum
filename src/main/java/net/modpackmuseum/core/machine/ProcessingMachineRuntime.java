package net.modpackmuseum.core.machine;

import net.modpackmuseum.core.energy.EnergyBuffer;

import java.util.Objects;

/**
 * Deterministic first-pass machine runtime for a single input slot and single output slot.
 *
 * <p>The tick is transactional at the recipe-step level: energy is only consumed when the
 * recipe can actually make progress, and input/output mutation happens together on completion.</p>
 */
public final class ProcessingMachineRuntime {
    private final MachineRecipe recipe;
    private final EnergyBuffer energy;
    private final int maxStackSize;

    private String inputItem;
    private int inputCount;
    private String outputItem;
    private int outputCount;
    private int progressTicks;

    public ProcessingMachineRuntime(MachineRecipe recipe, long energyCapacity) {
        this(recipe, energyCapacity, 64);
    }

    public ProcessingMachineRuntime(MachineRecipe recipe, long energyCapacity, int maxStackSize) {
        this.recipe = Objects.requireNonNull(recipe, "recipe");
        if (maxStackSize <= 0) {
            throw new IllegalArgumentException("maxStackSize must be > 0");
        }
        this.energy = new EnergyBuffer(energyCapacity);
        this.maxStackSize = maxStackSize;
    }

    public MachineRecipe recipe() {
        return recipe;
    }

    public EnergyBuffer energy() {
        return energy;
    }

    public int progressTicks() {
        return progressTicks;
    }

    public int inputCount() {
        return inputCount;
    }

    public int outputCount() {
        return outputCount;
    }

    public String inputItem() {
        return inputItem;
    }

    public String outputItem() {
        return outputItem;
    }

    public int insertInput(String itemId, int count) {
        requireCount(count);
        Objects.requireNonNull(itemId, "itemId");
        if (!recipe.inputItem().equals(itemId) || count == 0) {
            return 0;
        }
        if (inputCount > 0 && !itemId.equals(inputItem)) {
            return 0;
        }
        int accepted = Math.min(count, maxStackSize - inputCount);
        if (accepted > 0) {
            inputItem = itemId;
            inputCount += accepted;
        }
        return accepted;
    }

    public int extractOutput(int requested) {
        requireCount(requested);
        int extracted = Math.min(requested, outputCount);
        outputCount -= extracted;
        if (outputCount == 0) {
            outputItem = null;
        }
        return extracted;
    }

    /** Returns true only when this tick advanced recipe progress. */
    public boolean tick() {
        if (!canProcess()) {
            return false;
        }
        if (energy.stored() < recipe.energyPerTick()) {
            return false;
        }

        energy.extract(recipe.energyPerTick());
        progressTicks++;

        if (progressTicks >= recipe.durationTicks()) {
            inputCount -= recipe.inputCount();
            if (inputCount == 0) {
                inputItem = null;
            }
            outputItem = recipe.outputItem();
            outputCount += recipe.outputCount();
            progressTicks = 0;
        }
        return true;
    }

    public MachineSnapshot snapshot() {
        return new MachineSnapshot(
            MachineSnapshot.CURRENT_SCHEMA,
            energy.stored(),
            progressTicks,
            inputItem,
            inputCount,
            outputItem,
            outputCount
        );
    }

    public static ProcessingMachineRuntime restore(
        MachineRecipe recipe,
        long energyCapacity,
        int maxStackSize,
        MachineSnapshot snapshot
    ) {
        ProcessingMachineRuntime runtime = new ProcessingMachineRuntime(recipe, energyCapacity, maxStackSize);
        runtime.restoreState(snapshot);
        return runtime;
    }

    /** Restores committed state while preserving the identity of the backing energy buffer. */
    public void restoreState(MachineSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.energyStored() > energy.capacity()) {
            throw new IllegalArgumentException("persisted energy exceeds capacity");
        }
        if (snapshot.progressTicks() >= recipe.durationTicks()) {
            throw new IllegalArgumentException("persisted progress must be below recipe duration");
        }
        if (snapshot.inputCount() > maxStackSize || snapshot.outputCount() > maxStackSize) {
            throw new IllegalArgumentException("persisted stack exceeds max stack size");
        }
        if (snapshot.inputCount() > 0 && !recipe.inputItem().equals(snapshot.inputItem())) {
            throw new IllegalArgumentException("persisted input does not match recipe");
        }
        if (snapshot.progressTicks() > 0 && snapshot.inputCount() < recipe.inputCount()) {
            throw new IllegalArgumentException("persisted progress requires enough committed input");
        }
        if (snapshot.outputCount() > 0 && !recipe.outputItem().equals(snapshot.outputItem())) {
            throw new IllegalArgumentException("persisted output does not match recipe");
        }
        if (snapshot.outputCount() + recipe.outputCount() > maxStackSize && snapshot.progressTicks() > 0) {
            throw new IllegalArgumentException("persisted progress cannot complete into full output");
        }

        energy.extract(energy.stored());
        energy.receive(snapshot.energyStored());
        progressTicks = snapshot.progressTicks();
        inputItem = snapshot.inputItem();
        inputCount = snapshot.inputCount();
        outputItem = snapshot.outputItem();
        outputCount = snapshot.outputCount();
    }

    private boolean canProcess() {
        if (inputCount < recipe.inputCount() || !recipe.inputItem().equals(inputItem)) {
            return false;
        }
        if (outputCount == 0) {
            return recipe.outputCount() <= maxStackSize;
        }
        return recipe.outputItem().equals(outputItem)
            && outputCount + recipe.outputCount() <= maxStackSize;
    }

    private static void requireCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
    }
}
