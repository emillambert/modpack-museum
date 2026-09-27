package net.modpackmuseum.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.modpackmuseum.core.world.EnergyWorldCable;

/** Lightweight topology marker for the M2 cable proof. */
public final class CableBlockEntity extends BlockEntity implements EnergyWorldCable {
    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumBlockEntities.CABLE, pos, state);
    }
}
