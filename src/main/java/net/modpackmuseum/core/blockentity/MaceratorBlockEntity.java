package net.modpackmuseum.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modpackmuseum.core.container.MuseumContainer;
import net.modpackmuseum.core.content.MuseumItems;
import net.modpackmuseum.core.energy.EnergyBuffer;
import net.modpackmuseum.core.machine.MachineRecipe;
import net.modpackmuseum.core.machine.MachineSnapshot;
import net.modpackmuseum.core.machine.ProcessingMachineRuntime;
import net.modpackmuseum.core.world.EnergyWorldPort;

/** First in-world processing adapter: one Raw Iron becomes two Iron Dust. */
public final class MaceratorBlockEntity extends BlockEntity implements MuseumContainer, MenuProvider, EnergyWorldPort {
    public static final int CONTAINER_SIZE = 27;
    public static final int SCHEMA_VERSION = 1;
    public static final long ENERGY_CAPACITY = 4_000;
    public static final long MAX_INPUT_PER_TICK = 32;
    private static final String RAW_IRON_ID = "minecraft:raw_iron";
    private static final String IRON_DUST_ID = "modpack_museum:iron_dust";
    private static final MachineRecipe RAW_IRON_RECIPE = new MachineRecipe(
        "raw_iron_to_dust",
        RAW_IRON_ID,
        1,
        IRON_DUST_ID,
        2,
        200,
        2
    );

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private final ProcessingMachineRuntime runtime = new ProcessingMachineRuntime(RAW_IRON_RECIPE, ENERGY_CAPACITY);

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumBlockEntities.MACERATOR, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity entity) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = entity.commitInputFromInventory();
        if (entity.runtime.tick()) {
            changed = true;
        }
        if (entity.flushOutputToInventory()) {
            changed = true;
        }
        if (changed) {
            entity.setChanged();
        }
    }

    private boolean commitInputFromInventory() {
        ItemStack input = items.get(0);
        if (!input.is(Items.RAW_IRON) || runtime.inputCount() >= 64) {
            return false;
        }
        int accepted = runtime.insertInput(RAW_IRON_ID, Math.min(input.getCount(), 64 - runtime.inputCount()));
        if (accepted == 0) {
            return false;
        }
        input.shrink(accepted);
        return true;
    }

    private boolean flushOutputToInventory() {
        if (runtime.outputCount() == 0) {
            return false;
        }
        ItemStack output = items.get(1);
        if (!output.isEmpty() && !output.is(MuseumItems.IRON_DUST)) {
            return false;
        }
        int space = output.isEmpty() ? 64 : output.getMaxStackSize() - output.getCount();
        if (space <= 0) {
            return false;
        }
        int extracted = runtime.extractOutput(space);
        if (extracted == 0) {
            return false;
        }
        if (output.isEmpty()) {
            items.set(1, new ItemStack(MuseumItems.IRON_DUST, extracted));
        } else {
            output.grow(extracted);
        }
        return true;
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("museum_schema", SCHEMA_VERSION);
        ContainerHelper.saveAllItems(output, items);
        MachineSnapshot snapshot = runtime.snapshot();
        output.putLong("energy", snapshot.energyStored());
        output.putInt("progress", snapshot.progressTicks());
        output.putInt("committed_input", snapshot.inputCount());
        output.putInt("pending_output", snapshot.outputCount());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        int committedInput = input.getIntOr("committed_input", 0);
        int pendingOutput = input.getIntOr("pending_output", 0);
        runtime.restoreState(new MachineSnapshot(
            MachineSnapshot.CURRENT_SCHEMA,
            input.getLongOr("energy", 0L),
            input.getIntOr("progress", 0),
            committedInput == 0 ? null : RAW_IRON_ID,
            committedInput,
            pendingOutput == 0 ? null : IRON_DUST_ID,
            pendingOutput
        ));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.modpack_museum.macerator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public EnergyBuffer museumEnergyBuffer() {
        return runtime.energy();
    }

    @Override
    public long museumMaxInputPerTick() {
        return MAX_INPUT_PER_TICK;
    }

    @Override
    public long museumMaxOutputPerTick() {
        return 0;
    }
}
