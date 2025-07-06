package org.zamecki.astralis;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zamecki.astralis.command.PlanetCommand;
import org.zamecki.astralis.planet.PlanetRegistry;
import org.zamecki.astralis.player.PlayerRespawnHandler;

public class Astralis implements ModInitializer {
    public static final String MOD_ID = "astralis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Astralis mod - Planetary System");

        // Initialize our planet registry
        PlanetRegistry.init();

        // Initialize player respawn handler
        PlayerRespawnHandler.init();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PlanetCommand.register(dispatcher));

        LOGGER.info("Astralis mod initialized successfully!");
    }
}
