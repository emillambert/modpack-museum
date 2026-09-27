#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# Compiles/runs the Minecraft-independent correctness core without Gradle or external deps.
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

cat > "$tmp/PureCoreSmoke.java" <<'JAVA'
import net.modpackmuseum.core.energy.EnergyBuffer;
import net.modpackmuseum.core.energy.EnergyNetwork;
import net.modpackmuseum.core.energy.EnergyPort;
import net.modpackmuseum.core.energy.EnergyTransfer;
import net.modpackmuseum.core.energy.GridEnergyTopology;
import net.modpackmuseum.core.energy.GridPos;
import net.modpackmuseum.core.emc.EmcLedger;
import net.modpackmuseum.core.machine.GeneratorRuntime;
import net.modpackmuseum.core.machine.GeneratorSnapshot;
import net.modpackmuseum.core.machine.MachineRecipe;
import net.modpackmuseum.core.machine.MachineSnapshot;
import net.modpackmuseum.core.machine.ProcessingMachineRuntime;

public final class PureCoreSmoke {
    public static void main(String[] args) {
        testEnergyPrimitive();
        testEmcPrimitive();
        testMachinePersistence();
        testGridTopologyAdapter();
        testIndustrialLoopCore();
        System.out.println("Pure core smoke test: PASS");
    }

    private static void testEnergyPrimitive() {
        EnergyBuffer energy = new EnergyBuffer(100);
        check(energy.receive(80) == 80, "energy accepts offered amount");
        check(energy.receive(50) == 20, "energy clamps to capacity");
        check(energy.extract(140) == 100, "energy clamps extraction");
        check(energy.stored() == 0, "energy cannot become negative");

        EnergyBuffer source = new EnergyBuffer(100, 75);
        EnergyBuffer sink = new EnergyBuffer(50, 10);
        long total = source.stored() + sink.stored();
        check(EnergyTransfer.transfer(source, sink, 999) == 40, "transfer is capped by sink space");
        check(source.stored() + sink.stored() == total, "transfer conserves total energy");
    }

    private static void testEmcPrimitive() {
        EmcLedger emc = new EmcLedger();
        emc.credit(8192);
        check(!emc.tryDebit(8193), "insufficient EMC does not mutate balance");
        check(emc.balance() == 8192, "failed debit is atomic");
        check(emc.tryDebit(8192), "exact EMC debit succeeds");
        check(emc.balance() == 0, "EMC cannot become negative");
    }

    private static void testMachinePersistence() {
        MachineRecipe recipe = new MachineRecipe(
            "iron", "minecraft:raw_iron", 1, "modpack_museum:iron_dust", 2, 4, 5
        );
        ProcessingMachineRuntime machine = new ProcessingMachineRuntime(recipe, 100);
        check(machine.insertInput("minecraft:raw_iron", 2) == 2, "machine accepts matching input");
        machine.energy().receive(40);
        check(machine.tick(), "machine progresses with input and energy");
        check(machine.tick(), "machine progresses deterministically");

        MachineSnapshot snapshot = machine.snapshot();
        EnergyBuffer existingMachineBuffer = machine.energy();
        machine.restoreState(snapshot);
        check(machine.energy() == existingMachineBuffer, "machine in-place restore preserves energy-buffer identity");
        ProcessingMachineRuntime restored = ProcessingMachineRuntime.restore(recipe, 100, 64, snapshot);
        check(restored.snapshot().equals(snapshot), "machine snapshot round-trips exactly");
        check(restored.tick(), "restored machine continues progress");
        check(restored.tick(), "restored machine completes recipe");
        check(restored.inputCount() == 1, "one input is consumed exactly");
        check(restored.outputCount() == 2, "recipe output is produced exactly once");
    }

    private static void testGridTopologyAdapter() {
        EnergyBuffer source = new EnergyBuffer(100, 50);
        EnergyBuffer sink = new EnergyBuffer(100);
        GridEnergyTopology grid = new GridEnergyTopology();
        GridPos sourcePos = new GridPos(0, 64, 0);
        GridPos cablePos = new GridPos(1, 64, 0);
        GridPos sinkPos = new GridPos(2, 64, 0);
        grid.addPort(sourcePos, source, 0, 50);
        grid.addConductor(cablePos);
        grid.addPort(sinkPos, sink, 50, 0);
        check(grid.connected(sourcePos, sinkPos), "grid adapter connects axial neighbours");
        long rebuilds = grid.rebuildCount();
        grid.remove(cablePos);
        check(!grid.connected(sourcePos, sinkPos), "grid adapter invalidates removed cable immediately");
        check(grid.rebuildCount() == rebuilds + 1, "grid topology rebuilds after removal only");
    }

    private static void testIndustrialLoopCore() {
        GeneratorRuntime generator = new GeneratorRuntime(128, 16);
        generator.addBurnTicks(8);
        for (int i = 0; i < 4; i++) generator.tick();
        GeneratorSnapshot generatorSaved = generator.snapshot();
        EnergyBuffer existingGeneratorBuffer = generator.energy();
        generator.restoreState(generatorSaved);
        check(generator.energy() == existingGeneratorBuffer, "generator in-place restore preserves energy-buffer identity");
        generator = GeneratorRuntime.restore(128, 16, generatorSaved);
        check(generator.snapshot().equals(generatorSaved), "generator snapshot round-trips exactly");

        EnergyBuffer batbox = new EnergyBuffer(128);
        MachineRecipe recipe = new MachineRecipe(
            "iron", "minecraft:raw_iron", 1, "modpack_museum:iron_dust", 2, 4, 8
        );
        ProcessingMachineRuntime macerator = new ProcessingMachineRuntime(recipe, 64);
        macerator.insertInput("minecraft:raw_iron", 1);

        EnergyNetwork network = new EnergyNetwork();
        network.addPort(new EnergyPort("generator", generator.energy(), 0, 32));
        network.addConductor("cable-a");
        network.addPort(new EnergyPort("batbox", batbox, 32, 16));
        network.addConductor("cable-b");
        network.addPort(new EnergyPort("macerator", macerator.energy(), 16, 0));
        network.connect("generator", "cable-a");
        network.connect("cable-a", "batbox");
        network.connect("batbox", "cable-b");
        network.connect("cable-b", "macerator");

        long before = generator.energy().stored() + batbox.stored() + macerator.energy().stored();
        network.transferTick(8);
        long after = generator.energy().stored() + batbox.stored() + macerator.energy().stored();
        check(before == after, "industrial network conserves energy");
        check(network.rebuildCount() == 1, "topology is cached after first use");
        check(!network.connected("generator", "macerator"), "terminal BatBox does not bridge cable networks");
        check(network.connected("generator", "batbox"), "generator can reach BatBox input network");
        check(network.connected("batbox", "macerator"), "BatBox can reach output network");
        network.transferTick(8);
        check(network.rebuildCount() == 1, "unchanged topology does not rebuild");

        // Simulate a server restart after one processing tick: persist all state, rebuild topology,
        // then continue. No topology cache is assumed to survive process restart.
        network.transferTick(8);
        check(macerator.tick(), "macerator progresses before simulated restart");
        GeneratorSnapshot savedGenerator = generator.snapshot();
        long savedBatbox = batbox.stored();
        MachineSnapshot savedMacerator = macerator.snapshot();
        long conservedBeforeRestart = savedGenerator.energyStored() + savedBatbox + savedMacerator.energyStored();

        generator = GeneratorRuntime.restore(128, 16, savedGenerator);
        batbox = new EnergyBuffer(128, savedBatbox);
        macerator = ProcessingMachineRuntime.restore(recipe, 64, 64, savedMacerator);
        network = new EnergyNetwork();
        network.addPort(new EnergyPort("generator", generator.energy(), 0, 32));
        network.addConductor("cable-a");
        network.addPort(new EnergyPort("batbox", batbox, 32, 16));
        network.addConductor("cable-b");
        network.addPort(new EnergyPort("macerator", macerator.energy(), 16, 0));
        network.connect("generator", "cable-a");
        network.connect("cable-a", "batbox");
        network.connect("batbox", "cable-b");
        network.connect("cable-b", "macerator");
        long conservedAfterRestart = generator.energy().stored() + batbox.stored() + macerator.energy().stored();
        check(conservedBeforeRestart == conservedAfterRestart, "restart preserves stored network energy");

        for (int i = 0; i < 8 && macerator.outputCount() == 0; i++) {
            generator.tick();
            network.transferTick(8);
            macerator.tick();
        }
        check(macerator.outputCount() == 2, "generator/cable/batbox/macerator core loop completes after restart");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
JAVA

mapfile -t core_sources < <(find \
  src/main/java/net/modpackmuseum/core/energy \
  src/main/java/net/modpackmuseum/core/emc \
  src/main/java/net/modpackmuseum/core/machine \
  -name '*.java' -print | sort)

javac -d "$tmp/classes" "${core_sources[@]}" "$tmp/PureCoreSmoke.java"
java -cp "$tmp/classes" PureCoreSmoke
