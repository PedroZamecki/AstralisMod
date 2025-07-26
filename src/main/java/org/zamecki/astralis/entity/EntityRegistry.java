package org.zamecki.astralis.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static org.zamecki.astralis.Astralis.MOD_ID;

public class EntityRegistry {
    private static EntityType<?> registerLivingEntity(String name, EntityType.EntityFactory<? extends LivingEntity> factory, SpawnGroup group, DefaultAttributeContainer attributeContainer) {
        EntityType<? extends LivingEntity> entityType = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, name),
                EntityType.Builder.create(factory, group)
                .build(
                        RegistryKey.of(
                                Registries.ENTITY_TYPE.getKey(),
                                Identifier.of(MOD_ID, name))));

        FabricDefaultAttributeRegistry.register(entityType, attributeContainer);
        PolymerEntityUtils.registerType(entityType);

        return entityType;
    }

    public static EntityType<?> ALIEN_VILLAGER = registerLivingEntity(
            "alien_villager",
            AlienVillagerEntity::new,
            SpawnGroup.MISC,
            AlienVillagerEntity.createAlienVillagerAttributes().build()
    );

    public static void init() {
    }
}
