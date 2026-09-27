package net.modpackmuseum.core.content;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Base class for M2 functional blocks that intentionally use stable vanilla proxy visuals first. */
abstract class AbstractMuseumProxyBlock extends BaseEntityBlock implements PolymerBlock {
    private final BlockState polymerState;

    protected AbstractMuseumProxyBlock(Properties properties, BlockState polymerState) {
        super(properties);
        this.polymerState = polymerState;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return polymerState;
    }
}
