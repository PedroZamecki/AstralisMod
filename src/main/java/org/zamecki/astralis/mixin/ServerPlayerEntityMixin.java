package org.zamecki.astralis.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zamecki.astralis.player.PlayerPlanetData;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // Store the planet where the player died for respawn purposes
        Identifier currentDimension = player.getWorld().getRegistryKey().getValue();
        
        // Convert dimension to planet identifier
        Identifier deathPlanet;
        if (currentDimension.equals(Identifier.of("minecraft", "overworld"))) {
            deathPlanet = Identifier.of("minecraft", "planet");
        } else {
            deathPlanet = currentDimension;
        }
        
        // Set this as the respawn planet (player should respawn where they died)
        PlayerPlanetData.setDesignatedPlanet(player.getUuid(), deathPlanet);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"))
    private void onSetSpawnPoint(Identifier dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // Convert dimension to planet identifier
        Identifier planet;
        if (dimension.equals(Identifier.of("minecraft", "overworld"))) {
            planet = Identifier.of("minecraft", "planet");
        } else {
            planet = dimension;
        }
        
        // Set the spawn point for this specific planet
        if (pos != null) {
            PlayerPlanetData.setSpawnPoint(player.getUuid(), planet, pos, angle, 0.0f);
        }
    }
}
