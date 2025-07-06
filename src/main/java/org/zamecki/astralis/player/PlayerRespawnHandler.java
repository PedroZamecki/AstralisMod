package org.zamecki.astralis.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.planet.PlanetRegistry;

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
        // Overworld = "minecraft:planet"
        if (worldId.equals(Identifier.of("minecraft", "overworld"))) {
            return Identifier.of("minecraft", "planet");
        }
        if (PlanetRegistry.getAllPlanets().containsKey(worldId)) {
            return worldId;
        }
        // Fallback
        return Identifier.of("minecraft", "planet");
    }

    private static void handlePlayerRespawn(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        Identifier deathPlanet = DEATH_PLANET_MAP.get(playerId);
        if (deathPlanet == null) {
            deathPlanet = PlayerPlanetData.getDesignatedPlanet(playerId);
        }
        DEATH_PLANET_MAP.remove(playerId);

        PlayerPlanetData.SpawnPoint spawnPoint = PlayerPlanetData.getPlayerData(playerId).getSpawnPoint(deathPlanet);

        if (spawnPoint != null) {
            teleportToSpawnPoint(player, deathPlanet, spawnPoint);
            Astralis.LOGGER.info("Respawned player {} at custom spawn on planet {}", player.getName().getString(), deathPlanet);
        } else {
            setDefaultSpawnForPlanet(player, deathPlanet);
            Astralis.LOGGER.info("Respawned player {} at default spawn on planet {}", player.getName().getString(), deathPlanet);
        }
    }

    private static void teleportToSpawnPoint(ServerPlayerEntity player, Identifier planet, PlayerPlanetData.SpawnPoint spawnPoint) {
        ServerWorld world;
        if (player.getServer() == null) {
            Astralis.LOGGER.warn("Server is null for player {}, cannot teleport to spawn point", player.getName().getString());
            return;
        }
        // Overworld
        if (planet.equals(Identifier.of("minecraft", "planet"))) {
            world = player.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
        } else {
            var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, planet);
            world = player.getServer().getWorld(worldKey);
        }
        if (world != null) {
            BlockPos pos = spawnPoint.getPosition();
            player.teleport(world,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                    spawnPoint.getYaw(), spawnPoint.getPitch(), true);
            Astralis.LOGGER.info("Respawned player {} at planet {} spawn point {}", player.getName().getString(), planet, pos);
        }
    }

    private static void setDefaultSpawnForPlanet(ServerPlayerEntity player, Identifier planet) {
        ServerWorld world;
        if (player.getServer() == null) {
            Astralis.LOGGER.warn("Server is null for player {}, cannot set default spawn", player.getName().getString());
            return;
        }
        // Overworld
        if (planet.equals(Identifier.of("minecraft", "planet"))) {
            world = player.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
        } else {
            var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, planet);
            world = player.getServer().getWorld(worldKey);
        }
        if (world != null) {
            BlockPos worldSpawn = world.getSpawnPos();
            player.teleport(world,
                    worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5,
                    Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                    0.0f, 0.0f, true);
            PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, worldSpawn, 0.0f, 0.0f);
            Astralis.LOGGER.info("Set default spawn for player {} on planet {} at {}", player.getName().getString(), planet, worldSpawn);
        }
    }
    
    /**
     * Sets the player's current location as spawn point for the current planet
     */
    public static void setCurrentLocationAsSpawn(ServerPlayerEntity player) {
        Identifier currentDimension = player.getWorld().getRegistryKey().getValue();
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
