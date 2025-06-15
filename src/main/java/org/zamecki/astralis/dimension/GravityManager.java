package org.zamecki.astralis.dimension;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.planet.Planet;
import org.zamecki.astralis.planet.PlanetRegistry;

public class GravityManager {
    private static final Identifier GRAVITY_MODIFIER_ID = Identifier.of(Astralis.MOD_ID, "planet_gravity");
    private static final boolean CUSTOM_GRAVITY_ENABLED = true;

    public static void applyGravity(Entity entity, ServerWorld world) {
        if (!CUSTOM_GRAVITY_ENABLED) {
            return;
        }

        Planet planet = PlanetRegistry.getPlanetByWorld(world.getRegistryKey());
        if (planet == null) {
            removeGravityModifier(entity);
            return;
        }

        float gravityFactor = planet.gravity();
        
        if (applyGravityAttribute(entity, gravityFactor)) {
            return;
        }
        
        applyVelocityGravity(entity, gravityFactor);
    }

    private static boolean applyGravityAttribute(Entity entity, float gravityFactor) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        
        var gravityAttribute = livingEntity.getAttributeInstance(EntityAttributes.GRAVITY);
        if (gravityAttribute == null) {
            return false;
        }

        gravityAttribute.removeModifier(GRAVITY_MODIFIER_ID);

        double baseGravity = 0.08;
        double targetGravity = baseGravity * gravityFactor;
        double modifierValue = targetGravity - baseGravity;

        if (gravityFactor <= 0.0F) {
            modifierValue = -baseGravity;
        }

        EntityAttributeModifier gravityModifier = new EntityAttributeModifier(
                GRAVITY_MODIFIER_ID,
                modifierValue,
                EntityAttributeModifier.Operation.ADD_VALUE
        );
        
        gravityAttribute.addTemporaryModifier(gravityModifier);
        return true;
    }

    private static void applyVelocityGravity(Entity entity, float gravityFactor) {
        if (entity.isOnGround() || entity.isTouchingWater() || entity.hasNoGravity()) {
            return;
        }

        Vec3d velocity = entity.getVelocity();
        
        double targetGravity = -0.08 * gravityFactor;
        
        if (gravityFactor <= 0.0F) {
            targetGravity = 0.0;
        }
        
        double gravityCorrection = targetGravity - (-0.08);
        
        entity.setVelocity(velocity.x, velocity.y + gravityCorrection, velocity.z);
    }

    private static void removeGravityModifier(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            var gravityAttribute = livingEntity.getAttributeInstance(EntityAttributes.GRAVITY);
            if (gravityAttribute != null) {
                gravityAttribute.removeModifier(GRAVITY_MODIFIER_ID);
            }
        }
    }

    public static float getGravityFactor(String planetId) {
        Identifier id = Identifier.tryParse(planetId);
        if (id == null) {
            return 1.0F;
        }
        
        Planet planet = PlanetRegistry.getPlanet(id);
        return planet != null ? planet.gravity() : 1.0F;
    }

    public static void onDimensionChange(Entity entity, ServerWorld newWorld) {
        applyGravity(entity, newWorld);
    }
}
