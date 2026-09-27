package net.modpackmuseum.core.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.modpackmuseum.core.blockentity.BatBoxBlockEntity;

/** M2 energy storage shell; uses a vanilla Copper Block as its temporary proxy visual. */
public final class BatBoxBlock extends AbstractMuseumProxyBlock {
    public BatBoxBlock(Properties properties) {
        super(properties, Blocks.COPPER_BLOCK.defaultBlockState());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BatBoxBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatBoxBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BatBoxBlockEntity batBox) {
            player.displayClientMessage(
                Component.literal("BatBox: " + batBox.storedEnergy() + " / " + BatBoxBlockEntity.ENERGY_CAPACITY + " EU"),
                true
            );
        }
        return InteractionResult.SUCCESS;
    }
}
