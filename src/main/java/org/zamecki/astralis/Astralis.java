package org.zamecki.astralis;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zamecki.astralis.block.BlockRegistry;
import org.zamecki.astralis.command.PlanetCommand;
import org.zamecki.astralis.entity.EntityRegistry;
import org.zamecki.astralis.planet.PlanetRegistry;
import org.zamecki.astralis.player.PlayerRespawnHandler;

public class Astralis implements ModInitializer {
    public static final String MOD_ID = "astralis";
    public static final Logger LOGGER = LoggerFactory.getLogger("Astralis");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Astralis mod - Planetary System");

        // Initialize our planet registry
        PlanetRegistry.init();

        // Initialize block registry for custom blocks
        BlockRegistry.init();

        // Initialize entity registry
        EntityRegistry.init();

        // Initialize player respawn handler
        PlayerRespawnHandler.init();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlanetCommand.register(dispatcher);
        });

        if (!PolymerResourcePackUtils.addModAssets(MOD_ID)) {
            LOGGER.error("Failed to add mod assets to Polymer resource pack. Mod will not function correctly.");
        }

        LOGGER.info("Astralis mod initialized successfully!");
    }
}
