package net.modpackmuseum.core.energy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Minecraft-independent adapter from six-neighbour block coordinates to {@link EnergyNetwork}.
 *
 * <p>The Fabric layer can mirror loaded Generator/Cable/BatBox/Macerator positions into this
 * class on placement/load/removal/unload. No world scan is required during an unchanged tick.</p>
 */
public final class GridEnergyTopology {
    public enum Kind {
        CONDUCTOR,
        PORT
    }

    private record Node(String id, Kind kind) {
    }

    private final EnergyNetwork network = new EnergyNetwork();
    private final Map<GridPos, Node> nodes = new HashMap<>();

    public void addConductor(GridPos pos) {
        Objects.requireNonNull(pos, "pos");
        requireEmpty(pos);
        Node node = new Node(idFor(pos), Kind.CONDUCTOR);
        nodes.put(pos, node);
        network.addConductor(node.id());
        connectExistingNeighbors(pos, node);
    }

    public void addPort(
        GridPos pos,
        EnergyBuffer buffer,
        long maxInputPerTick,
        long maxOutputPerTick
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(buffer, "buffer");
        requireEmpty(pos);
        Node node = new Node(idFor(pos), Kind.PORT);
        nodes.put(pos, node);
        network.addPort(new EnergyPort(node.id(), buffer, maxInputPerTick, maxOutputPerTick));
        connectExistingNeighbors(pos, node);
    }

    public boolean remove(GridPos pos) {
        Node removed = nodes.remove(Objects.requireNonNull(pos, "pos"));
        if (removed == null) {
            return false;
        }
        network.removeVertex(removed.id());
        return true;
    }

    public boolean contains(GridPos pos) {
        return nodes.containsKey(pos);
    }

    public Kind kindAt(GridPos pos) {
        Node node = nodes.get(pos);
        return node == null ? null : node.kind();
    }

    public int size() {
        return nodes.size();
    }

    public boolean connected(GridPos a, GridPos b) {
        return network.connected(requireNode(a).id(), requireNode(b).id());
    }

    public long transferTick(int maxTransferOperations) {
        return network.transferTick(maxTransferOperations);
    }

    public long topologyVersion() {
        return network.topologyVersion();
    }

    public long rebuildCount() {
        return network.rebuildCount();
    }

    private void connectExistingNeighbors(GridPos pos, Node node) {
        for (GridPos neighborPos : pos.neighbors()) {
            Node neighbor = nodes.get(neighborPos);
            if (neighbor != null) {
                network.connect(node.id(), neighbor.id());
            }
        }
    }

    private void requireEmpty(GridPos pos) {
        if (nodes.containsKey(pos)) {
            throw new IllegalArgumentException("energy node already exists at " + pos);
        }
    }

    private Node requireNode(GridPos pos) {
        Node node = nodes.get(Objects.requireNonNull(pos, "pos"));
        if (node == null) {
            throw new IllegalArgumentException("no energy node at " + pos);
        }
        return node;
    }

    private static String idFor(GridPos pos) {
        return pos.x() + "," + pos.y() + "," + pos.z();
    }
}
