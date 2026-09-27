package net.modpackmuseum.core.blockentity;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.modpackmuseum.core.ModpackMuseum;
import net.modpackmuseum.core.content.MuseumBlocks;

/** Block-entity registrations for persistent server-authoritative machine state. */
public final class MuseumBlockEntities {
    public static final BlockEntityType<TestMachineBlockEntity> TEST_MACHINE = register(
        "test_machine",
        TestMachineBlockEntity::new,
        MuseumBlocks.TEST_MACHINE
    );

    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = register(
        "generator",
        GeneratorBlockEntity::new,
        MuseumBlocks.GENERATOR
    );

    public static final BlockEntityType<CableBlockEntity> CABLE = register(
        "cable",
        CableBlockEntity::new,
        MuseumBlocks.CABLE
    );

    public static final BlockEntityType<BatBoxBlockEntity> BATBOX = register(
        "batbox",
        BatBoxBlockEntity::new,
        MuseumBlocks.BATBOX
    );

    public static final BlockEntityType<MaceratorBlockEntity> MACERATOR = register(
        "macerator",
        MaceratorBlockEntity::new,
        MuseumBlocks.MACERATOR
    );

    private MuseumBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
        String name,
        FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
        Block... blocks
    ) {
        return Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ModpackMuseum.id(name),
            FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build()
        );
    }

    public static void initialize() {
        // Vanilla clients must never receive custom block-entity type ids they cannot decode.
        PolymerBlockUtils.registerBlockEntity(TEST_MACHINE, GENERATOR, CABLE, BATBOX, MACERATOR);
    }
}
