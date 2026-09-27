package net.modpackmuseum.core.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnergyBufferTest {
    @Test
    void conservesAndClampsEnergy() {
        EnergyBuffer buffer = new EnergyBuffer(100);
        assertEquals(80, buffer.receive(80));
        assertEquals(20, buffer.receive(50));
        assertEquals(100, buffer.stored());
        assertEquals(60, buffer.extract(60));
        assertEquals(40, buffer.stored());
        assertEquals(40, buffer.extract(999));
        assertEquals(0, buffer.stored());
    }

    @Test
    void rejectsNegativeTransfers() {
        EnergyBuffer buffer = new EnergyBuffer(100);
        assertThrows(IllegalArgumentException.class, () -> buffer.receive(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.extract(-1));
    }
}
