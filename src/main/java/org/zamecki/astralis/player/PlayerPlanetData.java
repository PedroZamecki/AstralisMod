package org.zamecki.astralis.player;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.zamecki.astralis.Astralis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data including designated planets and spawn points per planet
 */
public class PlayerPlanetData {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new HashMap<>();
    
    public static class PlayerData {
        private Identifier designatedPlanet;
        private final Map<Identifier, SpawnPoint> planetSpawns = new HashMap<>();
        
        public PlayerData(Identifier designatedPlanet) {
            this.designatedPlanet = designatedPlanet;
        }
        
        public Identifier getDesignatedPlanet() {
            return designatedPlanet;
        }
        
        public void setDesignatedPlanet(Identifier planet) {
            this.designatedPlanet = planet;
        }
        
        public void setSpawnPoint(Identifier planet, BlockPos pos, float yaw, float pitch) {
            planetSpawns.put(planet, new SpawnPoint(pos, yaw, pitch));
        }
        
        public SpawnPoint getSpawnPoint(Identifier planet) {
            return planetSpawns.get(planet);
        }
        
        public Map<Identifier, SpawnPoint> getAllSpawnPoints() {
            return new HashMap<>(planetSpawns);
        }
    }
    
    public static class SpawnPoint {
        private final BlockPos position;
        private final float yaw;
        private final float pitch;
        
        public SpawnPoint(BlockPos position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }
        
        public BlockPos getPosition() {
            return position;
        }
        
        public float getYaw() {
            return yaw;
        }
        
        public float getPitch() {
            return pitch;
        }
        
        public Vec3d getVec3d() {
            return Vec3d.ofCenter(position);
        }
    }
    
    /**
     * Gets or creates player data
     */
    public static PlayerData getPlayerData(UUID playerId) {
        return PLAYER_DATA.computeIfAbsent(playerId, id -> {
            // Default designated planet is minecraft:planet
            return new PlayerData(Identifier.of("minecraft", "planet"));
        });
    }
    
    /**
     * Gets or creates player data by player entity
     */
    public static PlayerData getPlayerData(ServerPlayerEntity player) {
        return getPlayerData(player.getUuid());
    }
    
    /**
     * Sets a player's designated planet
     */
    public static void setDesignatedPlanet(UUID playerId, Identifier planet) {
        PlayerData data = getPlayerData(playerId);
        data.setDesignatedPlanet(planet);
        Astralis.LOGGER.info("Player {} designated planet set to {}", playerId, planet);
    }
    
    /**
     * Sets a player's spawn point for a specific planet
     */
    public static void setSpawnPoint(UUID playerId, Identifier planet, BlockPos pos, float yaw, float pitch) {
        PlayerData data = getPlayerData(playerId);
        data.setSpawnPoint(planet, pos, yaw, pitch);
        Astralis.LOGGER.info("Player {} spawn point for planet {} set to {}", playerId, planet, pos);
    }
    
    /**
     * Gets a player's spawn point for their designated planet
     */
    public static SpawnPoint getDesignatedSpawnPoint(UUID playerId) {
        PlayerData data = getPlayerData(playerId);
        return data.getSpawnPoint(data.getDesignatedPlanet());
    }
    
    /**
     * Gets a player's designated planet
     */
    public static Identifier getDesignatedPlanet(UUID playerId) {
        return getPlayerData(playerId).getDesignatedPlanet();
    }
    
    /**
     * Saves player data to NBT
     */
    public static NbtCompound savePlayerData(UUID playerId) {
        PlayerData data = PLAYER_DATA.get(playerId);
        if (data == null) return new NbtCompound();
        
        NbtCompound nbt = new NbtCompound();
        nbt.putString("designated_planet", data.getDesignatedPlanet().toString());
        
        NbtCompound spawnsNbt = new NbtCompound();
        for (Map.Entry<Identifier, SpawnPoint> entry : data.getAllSpawnPoints().entrySet()) {
            NbtCompound spawnNbt = new NbtCompound();
            BlockPos pos = entry.getValue().getPosition();
            spawnNbt.putInt("x", pos.getX());
            spawnNbt.putInt("y", pos.getY());
            spawnNbt.putInt("z", pos.getZ());
            spawnNbt.putFloat("yaw", entry.getValue().getYaw());
            spawnNbt.putFloat("pitch", entry.getValue().getPitch());
            spawnsNbt.put(entry.getKey().toString(), spawnNbt);
        }
        nbt.put("spawns", spawnsNbt);
        
        return nbt;
    }
    
    /**
     * Loads player data from NBT
     */
    public static void loadPlayerData(UUID playerId, NbtCompound nbt) {
        if (nbt.isEmpty()) return;
        
        String designatedPlanetStr = nbt.getString("designated_planet").orElse("minecraft:planet");
        Identifier designatedPlanet = Identifier.tryParse(designatedPlanetStr);
        if (designatedPlanet == null) designatedPlanet = Identifier.of("minecraft", "planet");
        
        PlayerData data = new PlayerData(designatedPlanet);
        
        if (nbt.contains("spawns")) {
            nbt.getCompound("spawns").ifPresent(spawnsNbt -> {
                for (String planetKey : spawnsNbt.getKeys()) {
                    Identifier planetId = Identifier.tryParse(planetKey);
                    if (planetId != null && spawnsNbt.contains(planetKey)) {
                        spawnsNbt.getCompound(planetKey).ifPresent(spawnNbt -> {
                            int x = spawnNbt.getInt("x").orElse(0);
                            int y = spawnNbt.getInt("y").orElse(100);
                            int z = spawnNbt.getInt("z").orElse(0);
                            BlockPos pos = new BlockPos(x, y, z);
                            float yaw = spawnNbt.getFloat("yaw").orElse(0.0f);
                            float pitch = spawnNbt.getFloat("pitch").orElse(0.0f);
                            data.setSpawnPoint(planetId, pos, yaw, pitch);
                        });
                    }
                }
            });
        }
        
        PLAYER_DATA.put(playerId, data);
    }
    
    /**
     * Removes player data (cleanup)
     */
    public static void removePlayerData(UUID playerId) {
        PLAYER_DATA.remove(playerId);
    }
}
