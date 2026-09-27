package net.modpackmuseum.core;

import com.mojang.brigadier.Command;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.modpackmuseum.core.blockentity.MuseumBlockEntities;
import net.modpackmuseum.core.content.MuseumBlocks;
import net.modpackmuseum.core.content.MuseumItems;
import net.modpackmuseum.core.world.MuseumEnergyWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

/**
 * Bootstrap entrypoint for Modpack Museum.
 *
 * <p>The current implementation is intentionally a narrow technology preview: it proves that
 * a stock Minecraft client can receive project-owned assets and interact with custom server-side
 * items, a textured custom block, and a server-driven vanilla GUI.</p>
 */
public final class ModpackMuseum implements ModInitializer {
    public static final String MOD_ID = "modpack_museum";
    public static final String VERSION = "0.1.0-SNAPSHOT";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Starting Modpack Museum {}", VERSION);

        // The resource pack is part of the product contract: a vanilla client should see the
        // project-owned models/textures without installing a mod. Autohost handles delivery.
        PolymerResourcePackUtils.markAsRequired();
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        MuseumItems.initialize();
        MuseumBlocks.initialize();
        MuseumBlockEntities.initialize();
        MuseumEnergyWorld.initialize();
        registerCommands();

        LOGGER.info("Modpack Museum content registered: M1 proof + M2 Generator/Cable/BatBox/Macerator shells");
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("museum")
                .executes(context -> {
                    context.getSource().sendSuccess(
                        () -> Component.literal(
                            "Modpack Museum " + VERSION
                                + " | M1 preview | /give @s modpack_museum:archive_token"
                                + " | /give @s modpack_museum:test_machine"
                                + " | M2: generator/cable/batbox/macerator"
                        ),
                        false
                    );
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("status")
                    .executes(context -> {
                        context.getSource().sendSuccess(
                            () -> Component.literal(
                                "Compatibility target: Minecraft 26.2 / Fabric / Polymer / vanilla client"
                            ),
                            false
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        );
    }
}
