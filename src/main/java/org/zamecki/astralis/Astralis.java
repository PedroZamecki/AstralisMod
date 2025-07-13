package org.zamecki.astralis;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Astralis implements ModInitializer {
    public static final String MOD_ID = "astralis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Astralis mod - Planetary System");

        // TODO: Fully implement this.
        // Initialize our planet registry
        // PlanetRegistry.init();

        // TODO: Fully implement this.
        // Initialize player respawn handler
        // PlayerRespawnHandler.init();

        // TODO: Fully implement this.
        // Register commands
        // CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PlanetCommand.register(dispatcher));

        LOGGER.info("Astralis mod initialized successfully!");
    }
}
