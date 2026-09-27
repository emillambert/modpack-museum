package net.modpackmuseum.core.content;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.modpackmuseum.core.ModpackMuseum;

import java.util.function.Function;

/** Blocks in the compatibility-layer technology preview. */
public final class MuseumBlocks {
    public static final Block TEST_MACHINE = registerWithItem(
        "test_machine",
        TestMachineBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    public static final Block GENERATOR = registerWithItem(
        "generator",
        GeneratorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    public static final Block CABLE = registerWithItem(
        "cable",
        CableBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_COPPER)
    );

    public static final Block BATBOX = registerWithItem(
        "batbox",
        BatBoxBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );

    public static final Block MACERATOR = registerWithItem(
        "macerator",
        MaceratorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    private MuseumBlocks() {
    }

    private static <T extends Block> T registerWithItem(
        String name,
        Function<BlockBehaviour.Properties, T> blockFactory,
        BlockBehaviour.Properties properties
    ) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ModpackMuseum.id(name));
        T block = blockFactory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ModpackMuseum.id(name));
        PolymerBlockItem blockItem = new PolymerBlockItem(
            block,
            new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        return block;
    }

    /** Forces static registration during mod initialization. */
    public static void initialize() {
    }
}
