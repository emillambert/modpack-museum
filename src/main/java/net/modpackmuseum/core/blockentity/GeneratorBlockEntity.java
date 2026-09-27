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
import net.modpackmuseum.core.energy.EnergyBuffer;
import net.modpackmuseum.core.machine.GeneratorRuntime;
import net.modpackmuseum.core.machine.GeneratorSnapshot;
import net.modpackmuseum.core.world.EnergyWorldPort;

/** First playable fuel-to-energy Generator adapter. Balance values are provisional for M2. */
public final class GeneratorBlockEntity extends BlockEntity implements MuseumContainer, MenuProvider, EnergyWorldPort {
    public static final int CONTAINER_SIZE = 27;
    public static final int SCHEMA_VERSION = 1;
    public static final long ENERGY_CAPACITY = 10_000;
    public static final long ENERGY_PER_TICK = 10;
    public static final long MAX_OUTPUT_PER_TICK = 32;
    private static final long COAL_BURN_TICKS = 1600;

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private final GeneratorRuntime runtime = new GeneratorRuntime(ENERGY_CAPACITY, ENERGY_PER_TICK);

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumBlockEntities.GENERATOR, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity entity) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;
        ItemStack fuel = entity.items.get(0);
        if (entity.runtime.burnTicksRemaining() == 0 && fuel.is(Items.COAL)) {
            fuel.shrink(1);
            entity.runtime.addBurnTicks(COAL_BURN_TICKS);
            changed = true;
        }
        if (entity.runtime.tick() > 0) {
            changed = true;
        }
        if (changed) {
            entity.setChanged();
        }
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
        GeneratorSnapshot snapshot = runtime.snapshot();
        output.putLong("energy", snapshot.energyStored());
        output.putLong("burn_ticks", snapshot.burnTicksRemaining());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        runtime.restoreState(new GeneratorSnapshot(
            GeneratorSnapshot.CURRENT_SCHEMA,
            input.getLongOr("energy", 0L),
            input.getLongOr("burn_ticks", 0L)
        ));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.modpack_museum.generator");
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
        return 0;
    }

    @Override
    public long museumMaxOutputPerTick() {
        return MAX_OUTPUT_PER_TICK;
    }
}
