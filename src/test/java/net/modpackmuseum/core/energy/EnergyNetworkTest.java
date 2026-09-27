package net.modpackmuseum.core.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnergyNetworkTest {
    @Test
    void transfersAcrossCableAndConservesEnergy() {
        EnergyBuffer generator = new EnergyBuffer(100, 80);
        EnergyBuffer batbox = new EnergyBuffer(100);
        EnergyBuffer machine = new EnergyBuffer(100);

        EnergyNetwork network = new EnergyNetwork();
        network.addPort(new EnergyPort("generator", generator, 0, 32));
        network.addConductor("cable-a");
        network.addPort(new EnergyPort("batbox", batbox, 32, 16));
        network.addConductor("cable-b");
        network.addPort(new EnergyPort("macerator", machine, 16, 0));
        network.connect("generator", "cable-a");
        network.connect("cable-a", "batbox");
        network.connect("batbox", "cable-b");
        network.connect("cable-b", "macerator");

        long totalBefore = generator.stored() + batbox.stored() + machine.stored();
        long moved = network.transferTick(8);
        long totalAfter = generator.stored() + batbox.stored() + machine.stored();

        assertTrue(moved > 0);
        assertEquals(totalBefore, totalAfter);
        assertTrue(batbox.stored() > 0 || machine.stored() > 0);
    }

    @Test
    void topologyCacheRebuildsOnlyAfterMutation() {
        EnergyNetwork network = new EnergyNetwork();
        network.addConductor("a");
        network.addConductor("b");
        network.connect("a", "b");

        assertTrue(network.connected("a", "b"));
        assertEquals(1, network.rebuildCount());
        assertTrue(network.connected("a", "b"));
        assertEquals(1, network.rebuildCount());

        network.disconnect("a", "b");
        assertFalse(network.connected("a", "b"));
        assertEquals(2, network.rebuildCount());
    }

    @Test
    void perTickWorkIsBoundedByOperationLimit() {
        EnergyNetwork network = new EnergyNetwork();
        EnergyBuffer source = new EnergyBuffer(100, 100);
        EnergyBuffer sinkA = new EnergyBuffer(100);
        EnergyBuffer sinkB = new EnergyBuffer(100);
        network.addPort(new EnergyPort("source", source, 0, 100));
        network.addPort(new EnergyPort("sink-a", sinkA, 100, 0));
        network.addPort(new EnergyPort("sink-b", sinkB, 100, 0));
        network.connect("source", "sink-a");
        network.connect("source", "sink-b");

        assertEquals(100, network.transferTick(1));
        assertEquals(0, source.stored());
        assertTrue((sinkA.stored() == 100 && sinkB.stored() == 0)
            || (sinkB.stored() == 100 && sinkA.stored() == 0));
    }
    @Test
    void disconnectedCableStopsTransferAndInvalidatesCachedPath() {
        EnergyBuffer source = new EnergyBuffer(100, 50);
        EnergyBuffer sink = new EnergyBuffer(100);
        EnergyNetwork network = new EnergyNetwork();
        network.addPort(new EnergyPort("source", source, 0, 50));
        network.addConductor("cable");
        network.addPort(new EnergyPort("sink", sink, 50, 0));
        network.connect("source", "cable");
        network.connect("cable", "sink");

        assertTrue(network.connected("source", "sink"));
        network.disconnect("cable", "sink");
        assertFalse(network.connected("source", "sink"));
        assertEquals(0, network.transferTick(4));
        assertEquals(50, source.stored());
        assertEquals(0, sink.stored());
    }

    @Test
    void handlesFiveHundredConductorsWithOneCachedRebuild() {
        EnergyNetwork network = new EnergyNetwork();
        network.addPort(new EnergyPort("source", new EnergyBuffer(100, 100), 0, 100));
        String previous = "source";
        for (int i = 0; i < 500; i++) {
            String cable = "cable-" + i;
            network.addConductor(cable);
            network.connect(previous, cable);
            previous = cable;
        }
        EnergyBuffer sink = new EnergyBuffer(100);
        network.addPort(new EnergyPort("sink", sink, 100, 0));
        network.connect(previous, "sink");

        assertEquals(100, network.transferTick(4));
        assertEquals(100, sink.stored());
        assertEquals(1, network.rebuildCount());
        network.connected("source", "sink");
        assertEquals(1, network.rebuildCount());
    }


    @Test
    void terminalPortDoesNotBridgeTwoCableNetworks() {
        EnergyBuffer generator = new EnergyBuffer(100, 80);
        EnergyBuffer batbox = new EnergyBuffer(100);
        EnergyBuffer machine = new EnergyBuffer(100);

        EnergyNetwork network = new EnergyNetwork();
        network.addPort(new EnergyPort("generator", generator, 0, 32));
        network.addConductor("input-cable");
        network.addPort(new EnergyPort("batbox", batbox, 32, 16));
        network.addConductor("output-cable");
        network.addPort(new EnergyPort("macerator", machine, 16, 0));
        network.connect("generator", "input-cable");
        network.connect("input-cable", "batbox");
        network.connect("batbox", "output-cable");
        network.connect("output-cable", "macerator");

        assertTrue(network.connected("generator", "batbox"));
        assertTrue(network.connected("batbox", "macerator"));
        assertFalse(network.connected("generator", "macerator"));

        assertEquals(32, network.transferTick(8));
        assertEquals(48, generator.stored());
        assertEquals(32, batbox.stored());
        assertEquals(0, machine.stored());

        assertEquals(48, network.transferTick(8));
        assertEquals(16, machine.stored());
        assertEquals(48, batbox.stored());
    }

}
