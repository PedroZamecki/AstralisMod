package org.zamecki.astralis.planet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

/**
 * Represents a planet with its properties and dimension configuration
 * 
 * Planets are conceptual groups that can contain multiple dimensions:
 * - minecraft:planet encompasses vanilla dimensions (overworld, nether, end)
 * - Custom planets typically have their own main dimension
 */
public record Planet(
        float gravity,
        float distance,
        Identifier noiseSettings,
        Identifier dimensionType
) {
    public static final Codec<Planet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("gravity").forGetter(Planet::gravity),
            Codec.FLOAT.fieldOf("distance").forGetter(Planet::distance),
            Identifier.CODEC.fieldOf("noise_settings").forGetter(Planet::noiseSettings),
            Identifier.CODEC.fieldOf("dimension_type").forGetter(Planet::dimensionType)
    ).apply(instance, Planet::new));

    /**
     * Gets the world registry key for this planet
     */
    public RegistryKey<World> getWorldKey(Identifier planetId) {
        return RegistryKey.of(RegistryKeys.WORLD, planetId);
    }

    /**
     * Gets the dimension type registry key for this planet
     */
    public RegistryKey<DimensionType> getDimensionTypeKey() {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, dimensionType);
    }

    /**
     * Checks if this planet has zero gravity
     */
    public boolean isZeroGravity() {
        return gravity <= 0.0F;
    }

    /**
     * Checks if this planet has reduced gravity compared to Earth
     */
    public boolean hasReducedGravity() {
        return gravity < 1.0F;
    }
}
