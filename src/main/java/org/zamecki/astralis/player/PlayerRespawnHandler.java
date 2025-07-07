package org.zamecki.astralis.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.planet.PlanetRegistry;

/**
 * Handles player respawn logic for planet-based spawn points
 * Note: The actual respawn logic is handled by ServerPlayerEntityMixin
 * This class provides utility methods for spawn point management
 */
public class PlayerRespawnHandler {
    
    public static void init() {
        // No longer need event handlers since we use Mixin
        Astralis.LOGGER.info("Player respawn handler initialized (using Mixin-based approach)");
    }
    
    /**
     * Determine which planet the player is currently on
     * Planets are conceptual groups - minecraft:planet includes Overworld, Nether, End
     * Other planets are mapped by their world dimension
     */
    public static Identifier getCurrentPlanet(ServerPlayerEntity player) {
        Identifier worldId = player.getWorld().getRegistryKey().getValue();
        
        // Default planet includes all vanilla dimensions (overworld, nether, end)
        if (worldId.equals(Identifier.of("minecraft", "overworld")) || 
            worldId.equals(Identifier.of("minecraft", "the_nether")) || 
            worldId.equals(Identifier.of("minecraft", "the_end"))) {
            return Identifier.of("minecraft", "planet");
        }
        
        // Check if this world belongs to a custom planet
        if (PlanetRegistry.getAllPlanets().containsKey(worldId)) {
            return worldId;
        }
        
        // Fallback to default planet for any unknown dimensions
        return Identifier.of("minecraft", "planet");
    }
    
    /**
     * Find a safe spawn location near origin (0,0,0) with some variation
     * Similar to vanilla spawn radius behavior
     */
    public static BlockPos findSafeSpawnNearOrigin(ServerWorld world) {
        // Try to find a safe location within a small radius around world spawn
        BlockPos worldSpawn = world.getSpawnPos();
        
        // If world spawn is reasonable (not too far from origin), use it
        if (Math.abs(worldSpawn.getX()) < 100 && Math.abs(worldSpawn.getZ()) < 100) {
            return worldSpawn;
        }
        
        // Otherwise, find a safe location near 0,0,0
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = world.getRandom().nextInt(21) - 10; // -10 to 10
            int z = world.getRandom().nextInt(21) - 10; // -10 to 10
            int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);
            
            if (y > world.getBottomY()) {
                return new BlockPos(x, y + 1, z);
            }
        }
        
        // Fallback to world spawn
        return worldSpawn;
    }
    
    /**
     * Sets the player's current location as spawn point for the current planet
     */
    public static void setCurrentLocationAsSpawn(ServerPlayerEntity player) {
        // Get the planet the player is currently on (not just the dimension)
        Identifier planet = getCurrentPlanet(player);
        BlockPos pos = player.getBlockPos();
        PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, pos, player.getYaw(), player.getPitch());
        Astralis.LOGGER.info("Player {} set spawn point for planet {} at {}", player.getName().getString(), planet, pos);
    }
}
