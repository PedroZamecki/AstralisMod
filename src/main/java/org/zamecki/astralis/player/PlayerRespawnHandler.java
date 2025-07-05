package org.zamecki.astralis.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.dimension.PlanetDimensions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles player respawn logic for planet-based spawn points
 */
public class PlayerRespawnHandler {
    
    // Track where each player died so we know which planet to respawn them on
    private static final Map<UUID, Identifier> DEATH_PLANET_MAP = new HashMap<>();
    
    public static void init() {
        // Register death event to track which planet player died on
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Determine which planet the player died on
                Identifier deathPlanet = getCurrentPlanet(player);
                DEATH_PLANET_MAP.put(player.getUuid(), deathPlanet);
                Astralis.LOGGER.info("Player {} died on planet: {}", player.getName().getString(), deathPlanet);
            }
        });
        
        // Register player respawn event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                handlePlayerRespawn(newPlayer);
            }
        });
        
        Astralis.LOGGER.info("Player respawn handler initialized");
    }
    
    /**
     * Determine which planet the player is currently on
     */
    private static Identifier getCurrentPlanet(ServerPlayerEntity player) {
        Identifier worldId = player.getWorld().getRegistryKey().getValue();
        
        // Check if it's the overworld (our default "planet")
        if (worldId.equals(Identifier.of("minecraft", "overworld"))) {
            return Identifier.of("minecraft", "planet");
        }
        
        // Check if it's one of our planet dimensions
        for (Map.Entry<String, net.minecraft.registry.RegistryKey<World>> entry : PlanetDimensions.getPlanetDimensions().entrySet()) {
            if (entry.getValue().getValue().equals(worldId)) {
                return Identifier.of(Astralis.MOD_ID, entry.getKey());
            }
        }
        
        // Default fallback
        return Identifier.of("minecraft", "planet");
    }
    
    private static void handlePlayerRespawn(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        
        // Get the planet where the player died
        Identifier deathPlanet = DEATH_PLANET_MAP.get(playerId);
        if (deathPlanet == null) {
            // Fallback to designated planet if no death location tracked
            deathPlanet = PlayerPlanetData.getDesignatedPlanet(playerId);
        }
        
        // Clean up the death tracking
        DEATH_PLANET_MAP.remove(playerId);
        
        // Get the spawn point for that planet
        PlayerPlanetData.SpawnPoint spawnPoint = PlayerPlanetData.getPlayerData(playerId).getSpawnPoint(deathPlanet);
        
        if (spawnPoint != null) {
            // Teleport player to their planet-specific spawn point
            teleportToSpawnPoint(player, deathPlanet, spawnPoint);
            Astralis.LOGGER.info("Respawned player {} at custom spawn on planet {}", player.getName().getString(), deathPlanet);
        } else {
            // No specific spawn point set, use default for the planet
            setDefaultSpawnForPlanet(player, deathPlanet);
            Astralis.LOGGER.info("Respawned player {} at default spawn on planet {}", player.getName().getString(), deathPlanet);
        }
    }
    
    private static void teleportToSpawnPoint(ServerPlayerEntity player, Identifier planet, PlayerPlanetData.SpawnPoint spawnPoint) {
        // Determine the world to teleport to
        if (planet.equals(Identifier.of("minecraft", "planet"))) {
            // Default planet = overworld
            var overworld = player.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                BlockPos pos = spawnPoint.getPosition();
                player.teleport(overworld, 
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                    spawnPoint.getYaw(), spawnPoint.getPitch(), true);
                Astralis.LOGGER.info("Respawned player {} at planet {} spawn point {}", player.getName().getString(), planet, pos);
            }
        } else {
            // Planet dimension
            String planetName = planet.getPath();
            var worldKey = PlanetDimensions.getPlanetDimensions().get(planetName);
            if (worldKey != null) {
                var targetWorld = player.getServer().getWorld(worldKey);
                if (targetWorld != null) {
                    BlockPos pos = spawnPoint.getPosition();
                    player.teleport(targetWorld, 
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                        spawnPoint.getYaw(), spawnPoint.getPitch(), true);
                    Astralis.LOGGER.info("Respawned player {} at planet {} spawn point {}", player.getName().getString(), planet, pos);
                }
            }
        }
    }
    
    private static void setDefaultSpawnForPlanet(ServerPlayerEntity player, Identifier planet) {
        if (planet.equals(Identifier.of("minecraft", "planet"))) {
            // Use world spawn
            var overworld = player.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
            if (overworld != null) {
                BlockPos worldSpawn = overworld.getSpawnPos();
                player.teleport(overworld, 
                    worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5,
                    Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                    0.0f, 0.0f, true);
                
                // Set this as the player's spawn point for this planet
                PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, worldSpawn, 0.0f, 0.0f);
                
                Astralis.LOGGER.info("Set default spawn for player {} on planet {} at {}", player.getName().getString(), planet, worldSpawn);
            }
        } else {
            // Planet dimension - find safe spawn location
            String planetName = planet.getPath();
            var worldKey = PlanetDimensions.getPlanetDimensions().get(planetName);
            if (worldKey != null) {
                var targetWorld = player.getServer().getWorld(worldKey);
                if (targetWorld != null) {
                    // Find a safe spawn location
                    int x = 0, z = 0;
                    targetWorld.getChunk(x >> 4, z >> 4); // Load chunk
                    int y = targetWorld.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z) + 1;
                    if (y <= targetWorld.getBottomY()) {
                        y = targetWorld.getSeaLevel() + 10;
                    }
                    
                    BlockPos safeSpawn = new BlockPos(x, y, z);
                    player.teleport(targetWorld, x + 0.5, y, z + 0.5,
                        Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                        0.0f, 0.0f, true);
                    
                    // Set this as the player's spawn point for this planet
                    PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, safeSpawn, 0.0f, 0.0f);
                    
                    Astralis.LOGGER.info("Set default spawn for player {} on planet {} at {}", player.getName().getString(), planet, safeSpawn);
                }
            }
        }
    }
    
    /**
     * Sets the player's current location as spawn point for the current planet
     */
    public static void setCurrentLocationAsSpawn(ServerPlayerEntity player) {
        Identifier currentDimension = player.getWorld().getRegistryKey().getValue();
        
        // Convert dimension to planet
        Identifier planet;
        if (currentDimension.equals(Identifier.of("minecraft", "overworld"))) {
            planet = Identifier.of("minecraft", "planet");
        } else {
            planet = currentDimension;
        }
        
        BlockPos pos = player.getBlockPos();
        PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, pos, player.getYaw(), player.getPitch());
        
        Astralis.LOGGER.info("Player {} set spawn point for planet {} at {}", player.getName().getString(), planet, pos);
    }
}
