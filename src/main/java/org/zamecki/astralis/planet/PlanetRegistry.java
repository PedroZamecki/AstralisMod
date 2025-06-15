package org.zamecki.astralis.planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.zamecki.astralis.Astralis;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PlanetRegistry {
    private static final Map<Identifier, Planet> PLANETS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Identifier MARS_ID = Identifier.of(Astralis.MOD_ID, "mars");
    public static final Identifier MOON_ID = Identifier.of(Astralis.MOD_ID, "moon");
    public static final Identifier SPACE_ID = Identifier.of(Astralis.MOD_ID, "space");

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of(Astralis.MOD_ID, "planets");
            }

            @Override
            public void reload(ResourceManager manager) {
                loadPlanets(manager);
            }
        });

        registerDefaultPlanets();
    }

    private static void loadPlanets(ResourceManager manager) {
        PLANETS.clear();
        manager.findResources("planets", path -> path.getPath().endsWith(".json")).forEach((identifier, resource) -> {
            try (var reader = new InputStreamReader(resource.getInputStream())) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                Planet planet = Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                
                // Extract planet ID from resource path
                String path = identifier.getPath();
                String planetName = path.substring("planets/".length(), path.length() - ".json".length());
                Identifier planetId = Identifier.of(identifier.getNamespace(), planetName);
                
                PLANETS.put(planetId, planet);
                Astralis.LOGGER.info("Loaded planet: {} with gravity {}", planetId, planet.gravity());
            } catch (Exception e) {
                Astralis.LOGGER.error("Failed to load planet data from {}: {}", identifier, e.getMessage());
            }
        });

        if (PLANETS.isEmpty()) {
            registerDefaultPlanets();
        }
    }

    private static void registerDefaultPlanets() {
        // Mars: 38% Earth gravity, Mars-like terrain
        Planet mars = new Planet(
                0.38F,
                225000000F, // Distance from sun in km
                Identifier.of("minecraft", "mars"),
                Identifier.of(Astralis.MOD_ID, "mars")
        );
        PLANETS.put(MARS_ID, mars);

        // Moon: 16% Earth gravity, Moon-like terrain
        Planet moon = new Planet(
                0.16F,
                384400F, // Distance from Earth in km
                Identifier.of("minecraft", "moon"),
                Identifier.of(Astralis.MOD_ID, "moon")
        );
        PLANETS.put(MOON_ID, moon);

        // Space: Zero gravity, empty void
        Planet space = new Planet(
                0.0F,
                0F, // In space
                Identifier.of("minecraft", "space"),
                Identifier.of(Astralis.MOD_ID, "space")
        );
        PLANETS.put(SPACE_ID, space);

        Astralis.LOGGER.info("Registered {} default planets", PLANETS.size());
    }

    /**
     * Gets a planet by its identifier
     */
    public static Planet getPlanet(Identifier planetId) {
        return PLANETS.get(planetId);
    }

    /**
     * Gets a planet by world registry key
     */
    public static Planet getPlanetByWorld(RegistryKey<World> worldKey) {
        Identifier worldId = worldKey.getValue();
        return PLANETS.get(worldId);
    }

    /**
     * Gets all registered planets
     */
    public static Map<Identifier, Planet> getAllPlanets() {
        return new HashMap<>(PLANETS);
    }

    /**
     * Checks if a world is a planet
     */
    public static boolean isPlanet(RegistryKey<World> worldKey) {
        return getPlanetByWorld(worldKey) != null;
    }

    /**
     * Gets gravity factor for a world, defaults to 1.0 (Earth gravity) if not a planet
     */
    public static float getGravityFactor(RegistryKey<World> worldKey) {
        Planet planet = getPlanetByWorld(worldKey);
        return planet != null ? planet.gravity() : 1.0F;
    }
}
