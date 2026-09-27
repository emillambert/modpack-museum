package net.modpackmuseum.core.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingMachineRuntimeTest {
    private static final MachineRecipe MACERATION = new MachineRecipe(
        "iron_dust", "minecraft:raw_iron", 1, "modpack_museum:iron_dust", 2, 4, 5
    );

    @Test
    void consumesEnergyOnlyWhenProgressCanAdvance() {
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(MACERATION, 100);
        assertFalse(machine.tick());
        assertEquals(0, machine.energy().stored());

        machine.insertInput("minecraft:raw_iron", 1);
        assertFalse(machine.tick());
        assertEquals(0, machine.progressTicks());

        machine.energy().receive(20);
        assertTrue(machine.tick());
        assertEquals(15, machine.energy().stored());
        assertEquals(1, machine.progressTicks());
    }

    @Test
    void completesAtomicallyWithoutDuplicatingInput() {
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(MACERATION, 100);
        machine.insertInput("minecraft:raw_iron", 1);
        machine.energy().receive(20);
        for (int i = 0; i < 4; i++) {
            assertTrue(machine.tick());
        }
        assertEquals(0, machine.inputCount());
        assertEquals(2, machine.outputCount());
        assertEquals("modpack_museum:iron_dust", machine.outputItem());
        assertEquals(0, machine.energy().stored());
        assertFalse(machine.tick());
    }

    @Test
    void blocksBeforeSpendingEnergyWhenOutputCannotFit() {
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(MACERATION, 100, 2);
        machine.insertInput("minecraft:raw_iron", 2);
        machine.energy().receive(40);
        for (int i = 0; i < 4; i++) {
            assertTrue(machine.tick());
        }
        long before = machine.energy().stored();
        assertFalse(machine.tick());
        assertEquals(before, machine.energy().stored());
        assertEquals(1, machine.inputCount());
        assertEquals(2, machine.outputCount());
    }

    @Test
    void snapshotRestoresCommittedState() {
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(MACERATION, 100);
        machine.insertInput("minecraft:raw_iron", 3);
        machine.energy().receive(50);
        machine.tick();
        machine.tick();

        MachineSnapshot saved = machine.snapshot();
        ProcessingMachineRuntime restored = ProcessingMachineRuntime.restore(MACERATION, 100, 64, saved);
        assertEquals(saved, restored.snapshot());
        assertEquals(2, restored.progressTicks());
        assertEquals(3, restored.inputCount());
        assertEquals(40, restored.energy().stored());
    }
    @Test
    void inPlaceRestorePreservesEnergyBufferIdentity() {
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(MACERATION, 100);
        var buffer = machine.energy();
        MachineSnapshot snapshot = new MachineSnapshot(
            MachineSnapshot.CURRENT_SCHEMA, 25, 1,
            "minecraft:raw_iron", 1, null, 0
        );
        machine.restoreState(snapshot);
        assertSame(buffer, machine.energy());
        assertEquals(snapshot, machine.snapshot());
    }

}
