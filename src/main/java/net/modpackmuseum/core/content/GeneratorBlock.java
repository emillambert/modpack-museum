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
import net.modpackmuseum.core.blockentity.GeneratorBlockEntity;
import net.modpackmuseum.core.blockentity.MuseumBlockEntities;

/** M2 Generator shell; uses a vanilla Furnace as its temporary stock-client proxy visual. */
public final class GeneratorBlock extends AbstractMuseumProxyBlock {
    public GeneratorBlock(Properties properties) {
        super(properties, Blocks.FURNACE.defaultBlockState());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GeneratorBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, MuseumBlockEntities.GENERATOR, GeneratorBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GeneratorBlockEntity generator) {
            player.openMenu(generator);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof GeneratorBlockEntity generator) {
            Containers.dropContents(level, pos, generator);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
