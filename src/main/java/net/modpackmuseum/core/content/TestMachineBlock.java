package net.modpackmuseum.core.content;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.modpackmuseum.core.ModpackMuseum;
import net.modpackmuseum.core.blockentity.TestMachineBlockEntity;

/**
 * M1 compatibility machine with a Polymer visual and a persistent vanilla-menu backing store.
 */
public final class TestMachineBlock extends BaseEntityBlock implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public TestMachineBlock(Properties properties) {
        super(properties);

        BlockState requested = PolymerBlockResourceUtils.requestBlock(
            BlockModelType.FULL_BLOCK,
            PolymerBlockModel.of(ModpackMuseum.id("block/test_machine"))
        );
        this.polymerBlockState = requested != null ? requested : Blocks.IRON_BLOCK.defaultBlockState();

        if (requested == null) {
            ModpackMuseum.LOGGER.warn(
                "Polymer had no free FULL_BLOCK state for test_machine; using iron-block fallback"
            );
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TestMachineBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestMachineBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }

    @Override
    protected void affectNeighborsAfterRemoval(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        boolean movedByPiston
    ) {
        if (level.getBlockEntity(pos) instanceof TestMachineBlockEntity machine) {
            Containers.dropContents(level, pos, machine);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TestMachineBlockEntity machine) {
            player.openMenu(machine);
        }
        return InteractionResult.SUCCESS;
    }
}
