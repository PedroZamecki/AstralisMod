package org.zamecki.astralis.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.player.PlayerPlanetData;
import org.zamecki.astralis.planet.PlanetRegistry;

/**
 * Mixin to intercept player respawn logic and handle planet-based spawn points
 * This hooks into the vanilla respawn system at the right point
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    /**
     * Inject into getRespawnTarget to handle custom planet respawning
     * This is called BEFORE vanilla respawn logic, so we can override it
     */
    @Inject(method = "getRespawnTarget", at = @At("HEAD"), cancellable = true)
    private void onGetRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // Determine which planet the player is currently on
        Identifier currentPlanet = getCurrentPlanet(player);
        
        // For minecraft:planet (vanilla dimensions), let vanilla handle respawn completely
        // This includes beds, respawn anchors, and all vanilla respawn mechanics
        if (currentPlanet.equals(Identifier.of("minecraft", "planet"))) {
            Astralis.LOGGER.debug("Player {} respawning on default planet, using vanilla respawn system", player.getName().getString());
            return; // Let vanilla handle beds, respawn anchors, etc.
        }
        
        // For custom planets, use our spawn point system
        Astralis.LOGGER.debug("Player {} respawning on custom planet {}", player.getName().getString(), currentPlanet);
        
        PlayerPlanetData.SpawnPoint spawnPoint = PlayerPlanetData.getPlayerData(player).getSpawnPoint(currentPlanet);
        
        if (spawnPoint != null) {
            // Use existing spawn point, but validate it first
            ServerWorld targetWorld = getWorldForPlanet(player, currentPlanet);
            if (targetWorld != null) {
                BlockPos pos = spawnPoint.getPosition();
                
                // Validate the spawn point using vanilla algorithm
                BlockPos validatedPos = player.getWorldSpawnPos(targetWorld, pos);
                
                // If the validated position is the same as saved, it's safe
                if (validatedPos.equals(pos)) {
                    Vec3d spawnPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    
                    TeleportTarget target = new TeleportTarget(targetWorld, spawnPos, Vec3d.ZERO, 
                        spawnPoint.getYaw(), spawnPoint.getPitch(), postDimensionTransition);
                    
                    Astralis.LOGGER.info("Player {} respawning at validated custom spawn point on planet {} at {}", 
                        player.getName().getString(), currentPlanet, pos);
                    
                    cir.setReturnValue(target);
                    return;
                } else {
                    Astralis.LOGGER.warn("Player {} spawn point on planet {} at {} is not safe, vanilla found safer position at {}", 
                        player.getName().getString(), currentPlanet, pos, validatedPos);
                    
                    // Update the spawn point to the safer location
                    Vec3d spawnPos = new Vec3d(validatedPos.getX() + 0.5, validatedPos.getY(), validatedPos.getZ() + 0.5);
                    PlayerPlanetData.setSpawnPoint(player.getUuid(), currentPlanet, validatedPos, 0.0f, 0.0f);
                    
                    TeleportTarget target = new TeleportTarget(targetWorld, spawnPos, Vec3d.ZERO, 
                        0.0f, 0.0f, postDimensionTransition);
                    
                    Astralis.LOGGER.info("Player {} respawning at corrected spawn point on planet {} at {}", 
                        player.getName().getString(), currentPlanet, validatedPos);
                    
                    cir.setReturnValue(target);
                    return;
                }
            }
        }
        
        // No custom spawn point, use vanilla spawn finding algorithm for this custom planet
        ServerWorld targetWorld = getWorldForPlanet(player, currentPlanet);
        if (targetWorld != null) {
            // Use vanilla's sophisticated spawn finding algorithm
            BlockPos safeSpawn = player.getWorldSpawnPos(targetWorld, targetWorld.getSpawnPos());
            Vec3d spawnPos = new Vec3d(safeSpawn.getX() + 0.5, safeSpawn.getY(), safeSpawn.getZ() + 0.5);
            
            // Save this as the player's spawn point for future use
            PlayerPlanetData.setSpawnPoint(player.getUuid(), currentPlanet, safeSpawn, 0.0f, 0.0f);
            
            TeleportTarget target = new TeleportTarget(targetWorld, spawnPos, Vec3d.ZERO, 
                0.0f, 0.0f, postDimensionTransition);
            
            Astralis.LOGGER.info("Player {} respawning at vanilla-safe spawn on planet {} at {}", 
                player.getName().getString(), currentPlanet, safeSpawn);
            
            cir.setReturnValue(target);
        }
    }
    
    /**
     * Determine which planet the player is currently on
     * Planets are conceptual groups - minecraft:planet includes Overworld, Nether, End
     * Other planets are mapped by their world dimension
     */
    private static Identifier getCurrentPlanet(ServerPlayerEntity player) {
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
     * Get the world for a given planet
     * For minecraft:planet, we need to determine the appropriate world based on context
     * For custom planets, use their main dimension
     */
    private static ServerWorld getWorldForPlanet(ServerPlayerEntity player, Identifier planetId) {
        if (player.getServer() == null) {
            return null;
        }
        
        // For minecraft:planet, we need to choose an appropriate world
        // Since this is called during respawn, we should use the overworld as the default
        if (planetId.equals(Identifier.of("minecraft", "planet"))) {
            var overworldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, 
                Identifier.of("minecraft", "overworld"));
            return player.getServer().getWorld(overworldKey);
        }
        
        // For custom planets, get their main dimension
        var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, planetId);
        return player.getServer().getWorld(worldKey);
    }
}
