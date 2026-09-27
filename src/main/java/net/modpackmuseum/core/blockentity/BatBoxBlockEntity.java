package net.modpackmuseum.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modpackmuseum.core.energy.EnergyBuffer;
import net.modpackmuseum.core.world.EnergyWorldPort;

/** First persistent storage node in the M2 energy proof. */
public final class BatBoxBlockEntity extends BlockEntity implements EnergyWorldPort {
    public static final int SCHEMA_VERSION = 1;
    public static final long ENERGY_CAPACITY = 40_000;
    public static final long TRANSFER_PER_TICK = 32;

    private final EnergyBuffer energy = new EnergyBuffer(ENERGY_CAPACITY);

    public BatBoxBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumBlockEntities.BATBOX, pos, state);
    }

    public long storedEnergy() {
        return energy.stored();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("museum_schema", SCHEMA_VERSION);
        output.putLong("energy", energy.stored());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long persisted = input.getLongOr("energy", 0L);
        if (persisted < 0 || persisted > ENERGY_CAPACITY) {
            persisted = 0;
        }
        energy.extract(energy.stored());
        energy.receive(persisted);
    }

    @Override
    public EnergyBuffer museumEnergyBuffer() {
        return energy;
    }

    @Override
    public long museumMaxInputPerTick() {
        return TRANSFER_PER_TICK;
    }

    @Override
    public long museumMaxOutputPerTick() {
        return TRANSFER_PER_TICK;
    }
}
