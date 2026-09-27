package net.modpackmuseum.core.content;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.modpackmuseum.core.ModpackMuseum;

import java.util.function.Function;

/** First server-defined items used to prove the vanilla-client presentation path. */
public final class MuseumItems {
    public static final Item ARCHIVE_TOKEN = register(
        "archive_token",
        SimplePolymerItem::new,
        new Item.Properties().stacksTo(16)
    );

    public static final Item IRON_DUST = register(
        "iron_dust",
        SimplePolymerItem::new,
        new Item.Properties()
    );

    private MuseumItems() {
    }

    private static <T extends Item> T register(
        String name,
        Function<Item.Properties, T> itemFactory,
        Item.Properties properties
    ) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ModpackMuseum.id(name));
        T item = itemFactory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    /** Forces static registration during mod initialization. */
    public static void initialize() {
    }
}
