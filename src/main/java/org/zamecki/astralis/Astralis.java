package org.zamecki.astralis;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zamecki.astralis.block.BlockRegistry;
import org.zamecki.astralis.entity.EntityRegistry;
import org.zamecki.astralis.item.ItemRegistry;

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

        // Register server assets
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        // Register blocks
        BlockRegistry.init();

        // Register items
        ItemRegistry.init();

        // Register entities
        EntityRegistry.init();

        LOGGER.info("Astralis mod initialized successfully!");
    }
}
