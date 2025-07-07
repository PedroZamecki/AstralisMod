package org.zamecki.astralis.planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.RegistryKey;
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

        // No default planets - only load from data files
        // Note: minecraft:planet is virtual and contains vanilla dimensions (overworld, nether, end)
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

        Astralis.LOGGER.info("Loaded {} planets from data files", PLANETS.size());
    }

    /**
     * Gets a planet by its identifier
     */
    public static Planet getPlanet(Identifier planetId) {
        return PLANETS.get(planetId);
    }

    /**
     * Gets a planet by world registry key
     * Note: This is for custom planets only - minecraft:planet is handled differently
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
     * Checks if a world is a custom planet
     * Note: This doesn't include minecraft:planet which encompasses vanilla dimensions
     */
    public static boolean isPlanet(RegistryKey<World> worldKey) {
        return getPlanetByWorld(worldKey) != null;
    }

    /**
     * Gets gravity factor for a world, defaults to 1.0 (Earth gravity) if not a custom planet
     * Note: minecraft:planet (vanilla dimensions) always uses 1.0 gravity
     */
    public static float getGravityFactor(RegistryKey<World> worldKey) {
        Planet planet = getPlanetByWorld(worldKey);
        return planet != null ? planet.gravity() : 1.0F;
    }
}
