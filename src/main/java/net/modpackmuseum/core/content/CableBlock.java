package net.modpackmuseum.core.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.modpackmuseum.core.blockentity.CableBlockEntity;

/** M2 cable topology node; Cut Copper is the temporary full-block vanilla-client proxy visual. */
public final class CableBlock extends AbstractMuseumProxyBlock {
    public CableBlock(Properties properties) {
        super(properties, Blocks.CUT_COPPER.defaultBlockState());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CableBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }
}
