package org.zamecki.astralis.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static org.zamecki.astralis.Astralis.MOD_ID;

public class EntityRegistry {
    public static final EntityType<SkulkAnt> SKULK_ANT_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "skulk_ant"),
            EntityType.Builder.create(SkulkAnt::new, SpawnGroup.CREATURE)
                    .dimensions(1f, 2.5f).build(
                            RegistryKey.of(
                                    Registries.ENTITY_TYPE.getKey(),
                                    Identifier.of(MOD_ID, "skulk_ant")
                            )
                    ));

    public static void init() {
        PolymerEntityUtils.registerType(
                SKULK_ANT_ENTITY_TYPE
        );
    }
}
