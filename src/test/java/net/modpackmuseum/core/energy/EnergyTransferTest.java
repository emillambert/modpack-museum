package net.modpackmuseum.core.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnergyTransferTest {
    @Test
    void conservesEnergyAcrossBuffers() {
        EnergyBuffer source = new EnergyBuffer(100, 90);
        EnergyBuffer sink = new EnergyBuffer(50, 20);

        long totalBefore = source.stored() + sink.stored();
        assertEquals(30, EnergyTransfer.transfer(source, sink, 80));
        assertEquals(totalBefore, source.stored() + sink.stored());
        assertEquals(60, source.stored());
        assertEquals(50, sink.stored());
    }

    @Test
    void respectsLimitAndRejectsNegativeLimit() {
        EnergyBuffer source = new EnergyBuffer(100, 100);
        EnergyBuffer sink = new EnergyBuffer(100);
        assertEquals(12, EnergyTransfer.transfer(source, sink, 12));
        assertThrows(IllegalArgumentException.class, () -> EnergyTransfer.transfer(source, sink, -1));
    }
}
