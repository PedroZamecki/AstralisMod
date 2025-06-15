package org.zamecki.astralis.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zamecki.astralis.dimension.GravityManager;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        if (!entity.getWorld().isClient && entity.getWorld() instanceof ServerWorld) {
            // Only apply gravity on server to maintain compatibility with vanilla clients
            GravityManager.applyGravity(entity, (ServerWorld) entity.getWorld());
        }
    }
}
