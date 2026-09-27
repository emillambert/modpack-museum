package net.modpackmuseum.core.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.modpackmuseum.core.blockentity.MaceratorBlockEntity;
import net.modpackmuseum.core.blockentity.MuseumBlockEntities;

/** M2 Macerator shell; uses a vanilla Blast Furnace as its temporary proxy visual. */
public final class MaceratorBlock extends AbstractMuseumProxyBlock {
    public MaceratorBlock(Properties properties) {
        super(properties, Blocks.BLAST_FURNACE.defaultBlockState());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MaceratorBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MaceratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, MuseumBlockEntities.MACERATOR, MaceratorBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MaceratorBlockEntity macerator) {
            player.openMenu(macerator);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof MaceratorBlockEntity macerator) {
            Containers.dropContents(level, pos, macerator);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
