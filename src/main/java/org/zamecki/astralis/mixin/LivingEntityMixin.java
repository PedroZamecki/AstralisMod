package org.zamecki.astralis.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.planet.Planet;
import org.zamecki.astralis.planet.PlanetRegistry;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private static final Identifier GRAVITY_MODIFIER_ID = Identifier.of(Astralis.MOD_ID, "planet_gravity");

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity.getWorld().isClient || !(entity.getWorld() instanceof ServerWorld world)) return;

        var gravityAttribute = entity.getAttributeInstance(EntityAttributes.GRAVITY);
        if (gravityAttribute == null) return;

        Planet planet = PlanetRegistry.getPlanetByWorld(world.getRegistryKey());
        gravityAttribute.removeModifier(GRAVITY_MODIFIER_ID);

        if (planet == null) return;

        float gravityFactor = planet.gravity();
        double baseGravity = 0.08;
        double modifierValue = (gravityFactor <= 0.0F) ? -baseGravity : (baseGravity * gravityFactor - baseGravity);

        EntityAttributeModifier gravityModifier = new EntityAttributeModifier(
                GRAVITY_MODIFIER_ID,
                modifierValue,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        gravityAttribute.addTemporaryModifier(gravityModifier);
    }
}
