package net.modpackmuseum.core.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridEnergyTopologyTest {
    @Test
    void placementAutomaticallyConnectsSixNeighborNodes() {
        EnergyBuffer source = new EnergyBuffer(100, 50);
        EnergyBuffer sink = new EnergyBuffer(100);
        GridEnergyTopology grid = new GridEnergyTopology();

        GridPos sourcePos = new GridPos(0, 64, 0);
        GridPos cablePos = new GridPos(1, 64, 0);
        GridPos sinkPos = new GridPos(2, 64, 0);
        grid.addPort(sourcePos, source, 0, 50);
        grid.addConductor(cablePos);
        grid.addPort(sinkPos, sink, 50, 0);

        assertTrue(grid.connected(sourcePos, sinkPos));
        assertEquals(50, grid.transferTick(4));
        assertEquals(50, sink.stored());
    }

    @Test
    void removingCableImmediatelyInvalidatesCachedConnectivity() {
        EnergyBuffer source = new EnergyBuffer(100, 50);
        EnergyBuffer sink = new EnergyBuffer(100);
        GridEnergyTopology grid = new GridEnergyTopology();
        GridPos sourcePos = new GridPos(0, 0, 0);
        GridPos cablePos = new GridPos(1, 0, 0);
        GridPos sinkPos = new GridPos(2, 0, 0);
        grid.addPort(sourcePos, source, 0, 50);
        grid.addConductor(cablePos);
        grid.addPort(sinkPos, sink, 50, 0);

        assertTrue(grid.connected(sourcePos, sinkPos));
        long rebuilds = grid.rebuildCount();
        assertTrue(grid.remove(cablePos));
        assertFalse(grid.connected(sourcePos, sinkPos));
        assertEquals(rebuilds + 1, grid.rebuildCount());
        assertEquals(0, grid.transferTick(4));
    }

    @Test
    void nonAdjacentDiagonalNodesDoNotConnect() {
        GridEnergyTopology grid = new GridEnergyTopology();
        GridPos a = new GridPos(0, 0, 0);
        GridPos b = new GridPos(1, 1, 0);
        grid.addConductor(a);
        grid.addConductor(b);
        assertFalse(grid.connected(a, b));
    }

    @Test
    void portStillDoesNotBridgeOppositeCableRuns() {
        EnergyBuffer storage = new EnergyBuffer(100);
        GridEnergyTopology grid = new GridEnergyTopology();
        GridPos left = new GridPos(-1, 0, 0);
        GridPos center = new GridPos(0, 0, 0);
        GridPos right = new GridPos(1, 0, 0);
        grid.addConductor(left);
        grid.addPort(center, storage, 32, 32);
        grid.addConductor(right);

        assertTrue(grid.connected(left, center));
        assertTrue(grid.connected(center, right));
        assertFalse(grid.connected(left, right));
    }
}
