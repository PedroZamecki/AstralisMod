package org.zamecki.astralis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zamecki.astralis.player.PlayerPlanetData;
import org.zamecki.astralis.planet.PlanetRegistry;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Command handler for planet teleportation
 * Only provides teleportation functionality - respawn logic is handled elsewhere
 */
public class PlanetCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("planet")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("teleport")
                .then(argument("planet", IdentifierArgumentType.identifier())
                    .suggests((context, builder) -> {
                        // Add minecraft:planet (default) as an option
                        builder.suggest("minecraft:planet");
                        // Add all custom planets
                        PlanetRegistry.getAllPlanets().keySet().forEach(id -> builder.suggest(id.toString()));
                        return builder.buildFuture();
                    })
                    .executes(PlanetCommand::teleportToPlanet)
                )
            )
        );
    }

    private static int teleportToPlanet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        Identifier planetId = IdentifierArgumentType.getIdentifier(context, "planet");

        // Validate that the planet exists
        if (!planetId.equals(Identifier.of("minecraft", "planet")) && 
            !PlanetRegistry.getAllPlanets().containsKey(planetId)) {
            source.sendError(Text.literal("Planet not found: " + planetId));
            return 0;
        }

        // Determine target world
        RegistryKey<World> worldKey;
        if (planetId.equals(Identifier.of("minecraft", "planet"))) {
            // For minecraft:planet, teleport to overworld (main dimension)
            worldKey = World.OVERWORLD;
        } else {
            // For custom planets, teleport to their main dimension
            worldKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, planetId);
        }

        ServerWorld targetWorld = source.getServer().getWorld(worldKey);
        if (targetWorld == null) {
            source.sendError(Text.literal("World not found for planet: " + planetId));
            return 0;
        }

        // Check if player has a spawn point for this planet
        PlayerPlanetData.SpawnPoint spawnPoint = PlayerPlanetData.getPlayerData(player).getSpawnPoint(planetId);
        BlockPos targetPos;
        float yaw = 0.0f;
        float pitch = 0.0f;

        if (spawnPoint != null) {
            // Use existing spawn point, but validate it first
            BlockPos savedPos = spawnPoint.getPosition();
            BlockPos validatedPos = player.getWorldSpawnPos(targetWorld, savedPos);
            
            if (validatedPos.equals(savedPos)) {
                // Spawn point is safe
                targetPos = savedPos;
                yaw = spawnPoint.getYaw();
                pitch = spawnPoint.getPitch();
            } else {
                // Spawn point is not safe, use validated position and update
                targetPos = validatedPos;
                PlayerPlanetData.setSpawnPoint(player.getUuid(), planetId, validatedPos, 0.0f, 0.0f);
                source.sendFeedback(() -> Text.literal("Your spawn point was unsafe and has been corrected"), false);
            }
        } else {
            // Use the planet's world spawn point with vanilla validation
            BlockPos worldSpawn = targetWorld.getSpawnPos();
            targetPos = player.getWorldSpawnPos(targetWorld, worldSpawn);
            PlayerPlanetData.setSpawnPoint(player.getUuid(), planetId, targetPos, yaw, pitch);
        }

        // Teleport player
        player.teleport(targetWorld, 
                targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 
                Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z), 
                yaw, pitch, true);

        source.sendFeedback(() -> Text.literal("Teleported to planet: " + planetId), true);
        return 1;
    }
}
