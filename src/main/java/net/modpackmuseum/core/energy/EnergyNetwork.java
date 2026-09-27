package net.modpackmuseum.core.energy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Cached undirected conductor topology for the M2 cable proof.
 *
 * <p>Conductors are transit vertices. Machine/storage ports are terminal endpoints: a path may
 * end at a port but never passes through one to reach another conductor network. That distinction
 * prevents a storage block with two attached cable runs from accidentally becoming a zero-cost
 * cable bridge. Topology is rebuilt only after mutation, and per-tick transfer work is bounded by
 * the caller.</p>
 */
public final class EnergyNetwork {
    private final Map<String, Set<String>> adjacency = new HashMap<>();
    private final Map<String, EnergyPort> ports = new HashMap<>();

    /** Conductor vertex -> cached conductor component id. */
    private final Map<String, Integer> conductorComponents = new HashMap<>();
    /** Port id -> conductor component ids directly touching that terminal port. */
    private final Map<String, Set<Integer>> portComponents = new HashMap<>();

    private long topologyVersion;
    private long cacheVersion = -1;
    private long rebuildCount;

    public void addConductor(String id) {
        addVertex(id);
    }

    public void addPort(EnergyPort port) {
        Objects.requireNonNull(port, "port");
        String id = port.id();
        if (ports.containsKey(id)) {
            throw new IllegalArgumentException("energy port already exists: " + id);
        }
        if (adjacency.containsKey(id)) {
            throw new IllegalArgumentException("vertex already exists as a conductor: " + id);
        }
        adjacency.put(id, new HashSet<>());
        ports.put(id, port);
        invalidateTopology();
    }

    public void removeVertex(String id) {
        Set<String> neighbors = adjacency.remove(id);
        if (neighbors == null) {
            return;
        }
        for (String neighbor : neighbors) {
            Set<String> entries = adjacency.get(neighbor);
            if (entries != null) {
                entries.remove(id);
            }
        }
        ports.remove(id);
        invalidateTopology();
    }

    public void connect(String a, String b) {
        requireExisting(a);
        requireExisting(b);
        if (a.equals(b)) {
            throw new IllegalArgumentException("self connections are not allowed");
        }
        boolean changed = adjacency.get(a).add(b);
        changed |= adjacency.get(b).add(a);
        if (changed) {
            invalidateTopology();
        }
    }

    public void disconnect(String a, String b) {
        requireExisting(a);
        requireExisting(b);
        boolean changed = adjacency.get(a).remove(b);
        changed |= adjacency.get(b).remove(a);
        if (changed) {
            invalidateTopology();
        }
    }

    /**
     * Returns whether two vertices are connected without using an intermediate machine/storage
     * port as a transit vertex.
     */
    public boolean connected(String a, String b) {
        requireExisting(a);
        requireExisting(b);
        ensureTopologyCache();
        return reachableWithoutPortTransit(a, b);
    }

    public long rebuildCount() {
        return rebuildCount;
    }

    public long topologyVersion() {
        return topologyVersion;
    }

    /**
     * Runs deterministic, bounded transfers and returns total energy moved.
     * A transfer operation means one source-to-sink pair that actually moved energy.
     */
    public long transferTick(int maxTransferOperations) {
        if (maxTransferOperations < 0) {
            throw new IllegalArgumentException("maxTransferOperations must be >= 0");
        }
        if (maxTransferOperations == 0) {
            return 0;
        }

        ensureTopologyCache();
        List<String> portIds = new ArrayList<>(ports.keySet());
        Collections.sort(portIds);
        Map<String, Long> inputRemaining = new HashMap<>();
        Map<String, Long> outputRemaining = new HashMap<>();
        for (String id : portIds) {
            EnergyPort port = ports.get(id);
            inputRemaining.put(id, port.maxInputPerTick());
            outputRemaining.put(id, port.maxOutputPerTick());
        }

        int operations = 0;
        long movedTotal = 0;
        for (String sourceId : portIds) {
            EnergyPort source = ports.get(sourceId);
            long sourceRemaining = outputRemaining.get(sourceId);
            if (sourceRemaining == 0 || source.buffer().stored() == 0) {
                continue;
            }

            for (String sinkId : portIds) {
                if (operations >= maxTransferOperations) {
                    return movedTotal;
                }
                if (sourceId.equals(sinkId) || !reachableWithoutPortTransit(sourceId, sinkId)) {
                    continue;
                }

                EnergyPort sink = ports.get(sinkId);
                long sinkRemaining = inputRemaining.get(sinkId);
                if (sinkRemaining == 0 || sink.buffer().space() == 0) {
                    continue;
                }

                long limit = Math.min(sourceRemaining, sinkRemaining);
                long moved = EnergyTransfer.transfer(source.buffer(), sink.buffer(), limit);
                if (moved > 0) {
                    operations++;
                    movedTotal = Math.addExact(movedTotal, moved);
                    sourceRemaining -= moved;
                    inputRemaining.put(sinkId, sinkRemaining - moved);
                    outputRemaining.put(sourceId, sourceRemaining);
                    if (sourceRemaining == 0 || source.buffer().stored() == 0) {
                        break;
                    }
                }
            }
        }
        return movedTotal;
    }

    private void addVertex(String id) {
        validateId(id);
        if (adjacency.containsKey(id)) {
            throw new IllegalArgumentException("energy vertex already exists: " + id);
        }
        adjacency.put(id, new HashSet<>());
        invalidateTopology();
    }

    private static void validateId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    private void requireExisting(String id) {
        if (!adjacency.containsKey(id)) {
            throw new IllegalArgumentException("unknown energy vertex: " + id);
        }
    }

    private void invalidateTopology() {
        topologyVersion++;
        conductorComponents.clear();
        portComponents.clear();
        cacheVersion = -1;
    }

    private boolean reachableWithoutPortTransit(String a, String b) {
        if (a.equals(b)) {
            return true;
        }

        boolean aPort = ports.containsKey(a);
        boolean bPort = ports.containsKey(b);

        if (aPort && bPort) {
            if (adjacency.get(a).contains(b)) {
                return true;
            }
            return intersects(portComponents.getOrDefault(a, Set.of()), portComponents.getOrDefault(b, Set.of()));
        }

        if (!aPort && !bPort) {
            return Objects.equals(conductorComponents.get(a), conductorComponents.get(b));
        }

        String portId = aPort ? a : b;
        String conductorId = aPort ? b : a;
        Integer component = conductorComponents.get(conductorId);
        return component != null && portComponents.getOrDefault(portId, Set.of()).contains(component);
    }

    private static boolean intersects(Set<Integer> left, Set<Integer> right) {
        Set<Integer> smaller = left.size() <= right.size() ? left : right;
        Set<Integer> larger = left.size() <= right.size() ? right : left;
        for (Integer value : smaller) {
            if (larger.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private void ensureTopologyCache() {
        if (cacheVersion == topologyVersion) {
            return;
        }

        conductorComponents.clear();
        portComponents.clear();

        List<String> conductors = new ArrayList<>();
        for (String id : adjacency.keySet()) {
            if (!ports.containsKey(id)) {
                conductors.add(id);
            }
        }
        Collections.sort(conductors);

        int nextComponent = 0;
        for (String start : conductors) {
            if (conductorComponents.containsKey(start)) {
                continue;
            }

            int componentId = nextComponent++;
            ArrayDeque<String> queue = new ArrayDeque<>();
            queue.add(start);
            conductorComponents.put(start, componentId);

            while (!queue.isEmpty()) {
                String current = queue.removeFirst();
                List<String> neighbors = new ArrayList<>(adjacency.get(current));
                Collections.sort(neighbors);
                for (String neighbor : neighbors) {
                    if (ports.containsKey(neighbor)) {
                        portComponents.computeIfAbsent(neighbor, ignored -> new HashSet<>()).add(componentId);
                    } else if (!conductorComponents.containsKey(neighbor)) {
                        conductorComponents.put(neighbor, componentId);
                        queue.addLast(neighbor);
                    }
                }
            }
        }

        for (String portId : ports.keySet()) {
            portComponents.computeIfAbsent(portId, ignored -> new HashSet<>());
        }

        cacheVersion = topologyVersion;
        rebuildCount++;
    }
}
