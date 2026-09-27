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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modpackmuseum.core.ModpackMuseum;
import net.modpackmuseum.core.container.MuseumContainer;

/**
 * Persistent replacement for the M1 transient test inventory.
 *
 * <p>This is intentionally still a generic compatibility machine, not the Macerator. It proves
 * that a vanilla chest menu can be backed by world-saved block-entity state before M2 content
 * starts depending on persistence.</p>
 */
public final class TestMachineBlockEntity extends BlockEntity implements MuseumContainer, MenuProvider {
    public static final int CONTAINER_SIZE = 27;
    public static final int SCHEMA_VERSION = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public TestMachineBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumBlockEntities.TEST_MACHINE, pos, state);
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int schema = input.getIntOr("museum_schema", SCHEMA_VERSION);
        if (schema != SCHEMA_VERSION) {
            ModpackMuseum.LOGGER.error(
                "Unknown test_machine persistence schema {} at {}; loading only the compatible inventory payload",
                schema,
                getBlockPos()
            );
        }
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.modpack_museum.test_machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
