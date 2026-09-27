package net.modpackmuseum.core.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorRuntimeTest {
    @Test
    void turnsCommittedBurnTimeIntoBoundedEnergy() {
        GeneratorRuntime generator = new GeneratorRuntime(10, 4);
        generator.addBurnTicks(3);
        assertEquals(4, generator.tick());
        assertEquals(4, generator.tick());
        assertEquals(2, generator.tick());
        assertEquals(10, generator.energy().stored());
        assertEquals(0, generator.burnTicksRemaining());
    }

    @Test
    void pausesFuelWhenBufferIsFull() {
        GeneratorRuntime generator = new GeneratorRuntime(4, 4);
        generator.addBurnTicks(2);
        assertEquals(4, generator.tick());
        assertEquals(1, generator.burnTicksRemaining());
        assertEquals(0, generator.tick());
        assertEquals(1, generator.burnTicksRemaining());
    }

    @Test
    void snapshotRestoresEnergyAndCommittedBurnTime() {
        GeneratorRuntime generator = new GeneratorRuntime(100, 7);
        generator.addBurnTicks(10);
        generator.tick();
        generator.tick();

        GeneratorSnapshot snapshot = generator.snapshot();
        GeneratorRuntime restored = GeneratorRuntime.restore(100, 7, snapshot);

        assertEquals(snapshot, restored.snapshot());
        assertEquals(14, restored.energy().stored());
        assertEquals(8, restored.burnTicksRemaining());
        assertEquals(7, restored.tick());
    }

    @Test
    void restoreRejectsEnergyAboveCapacity() {
        GeneratorSnapshot snapshot = new GeneratorSnapshot(GeneratorSnapshot.CURRENT_SCHEMA, 11, 1);
        assertThrows(IllegalArgumentException.class, () -> GeneratorRuntime.restore(10, 4, snapshot));
    }

    @Test
    void inPlaceRestorePreservesEnergyBufferIdentity() {
        GeneratorRuntime generator = new GeneratorRuntime(100, 5);
        var buffer = generator.energy();
        generator.restoreState(new GeneratorSnapshot(GeneratorSnapshot.CURRENT_SCHEMA, 35, 9));
        assertSame(buffer, generator.energy());
        assertEquals(35, generator.energy().stored());
        assertEquals(9, generator.burnTicksRemaining());
    }

}
