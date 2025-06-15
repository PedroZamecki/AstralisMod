package org.zamecki.astralis.dimension;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.planet.PlanetRegistry;

import java.util.HashMap;
import java.util.Map;

public class PlanetDimensions {
    private static final Map<String, RegistryKey<World>> PLANET_DIMENSIONS = new HashMap<>();

    // Planet registry keys
    public static final String MOON_ID = "moon";
    public static final String MARS_ID = "mars";
    public static final String SPACE_ID = "space";

    public static final RegistryKey<World> MOON = registerPlanet(MOON_ID);
    public static final RegistryKey<World> MARS = registerPlanet(MARS_ID);
    public static final RegistryKey<World> SPACE = registerPlanet(SPACE_ID);

    // Registry keys for dimension types
    public static final RegistryKey<DimensionType> MOON_TYPE = RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE, Identifier.of(Astralis.MOD_ID, MOON_ID));
    public static final RegistryKey<DimensionType> MARS_TYPE = RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE, Identifier.of(Astralis.MOD_ID, MARS_ID));
    public static final RegistryKey<DimensionType> SPACE_TYPE = RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE, Identifier.of(Astralis.MOD_ID, SPACE_ID));

    public static RegistryKey<World> registerPlanet(String id) {
        RegistryKey<World> planetKey = RegistryKey.of(
                RegistryKeys.WORLD, Identifier.of(Astralis.MOD_ID, id));
        PLANET_DIMENSIONS.put(id, planetKey);
        return planetKey;
    }

    public static Map<String, RegistryKey<World>> getPlanetDimensions() {
        return PLANET_DIMENSIONS;
    }

    public static void init() {
        // Initialize the planet registry first
        PlanetRegistry.init();
        
        // This method is called during mod initialization to ensure all static initializers are run
        Astralis.LOGGER.info("Initializing planet dimensions");
    }
}
