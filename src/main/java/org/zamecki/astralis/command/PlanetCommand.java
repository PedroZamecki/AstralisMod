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
import org.zamecki.astralis.dimension.PlanetDimensions;
import org.zamecki.astralis.player.PlayerPlanetData;
import org.zamecki.astralis.player.PlayerRespawnHandler;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.zamecki.astralis.Astralis.MOD_ID;

public class PlanetCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("planet")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("teleport")
                .then(argument("planet", IdentifierArgumentType.identifier())
                    .suggests((context, builder) -> {
                        for (String planetId : PlanetDimensions.getPlanetDimensions().keySet()) {
                            builder.suggest(MOD_ID + ":" + planetId);
                        }
                        // Add overworld as an option for returning to Earth
                        builder.suggest("minecraft:planet");
                        return builder.buildFuture();
                    })
                    .executes(PlanetCommand::teleportToPlanet)
                )
            )
            .then(literal("return")
                .executes(PlanetCommand::returnToDesignatedPlanet)
            )
            .then(literal("setspawn")
                .executes(PlanetCommand::setCurrentSpawn)
            )
            .then(literal("designate")
                .then(argument("planet", IdentifierArgumentType.identifier())
                    .suggests((context, builder) -> {
                        for (String planetId : PlanetDimensions.getPlanetDimensions().keySet()) {
                            builder.suggest(MOD_ID + ":" + planetId);
                        }
                        builder.suggest("minecraft:planet");
                        return builder.buildFuture();
                    })
                    .executes(PlanetCommand::designatePlanet)
                )
            )
        );
    }

    private static int teleportToPlanet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        Identifier planetId = IdentifierArgumentType.getIdentifier(context, "planet");

        return teleportToPlanetInternal(source, player, planetId, "Teleported to planet: " + planetId);
    }

    private static BlockPos findSafeSpawnLocation(ServerWorld world, Identifier planetId) {
        if (planetId.equals(Identifier.of("minecraft", "planet"))) {
            return world.getSpawnPos();
        } else {
            // Find safe location for planet dimensions
            int x = 0, z = 0;
            world.getChunk(x >> 4, z >> 4); // Load chunk
            int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z) + 1;
            if (y <= world.getBottomY()) {
                y = world.getSeaLevel() + 10;
            }
            return new BlockPos(x, y, z);
        }
    }

    private static int returnToDesignatedPlanet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        // Get player's designated planet
        Identifier designatedPlanet = PlayerPlanetData.getDesignatedPlanet(player.getUuid());
        
        // Simply teleport to the designated planet (reuses existing logic)
        return teleportToPlanetInternal(source, player, designatedPlanet, "Returned to designated planet: " + designatedPlanet);
    }

    private static int teleportToPlanetInternal(ServerCommandSource source, ServerPlayerEntity player, Identifier planetId, String successMessage) throws CommandSyntaxException {
        // Determine target world
        RegistryKey<World> worldKey;
        if (planetId.equals(Identifier.of("minecraft", "planet"))) {
            worldKey = World.OVERWORLD;
        } else if (planetId.getNamespace().equals(MOD_ID)) {
            worldKey = PlanetDimensions.getPlanetDimensions().get(planetId.getPath());
            if (worldKey == null) {
                source.sendError(Text.literal("Unknown planet: " + planetId.getPath()));
                return 0;
            }
        } else {
            source.sendError(Text.literal("Invalid planet: " + planetId));
            return 0;
        }

        ServerWorld targetWorld = source.getServer().getWorld(worldKey);
        if (targetWorld == null) {
            source.sendError(Text.literal("Planet world not loaded: " + planetId));
            return 0;
        }

        // Get spawn point or use default
        PlayerPlanetData.SpawnPoint spawnPoint = PlayerPlanetData.getPlayerData(player.getUuid()).getSpawnPoint(planetId);
        BlockPos teleportPos;
        float yaw = 0.0f, pitch = 0.0f;
        
        if (spawnPoint != null) {
            teleportPos = spawnPoint.getPosition();
            yaw = spawnPoint.getYaw();
            pitch = spawnPoint.getPitch();
        } else {
            teleportPos = findSafeSpawnLocation(targetWorld, planetId);
        }
        
        // Teleport player
        player.teleport(targetWorld, teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5,
            Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z), yaw, pitch, true);

        source.sendFeedback(() -> Text.literal(successMessage), true);
        return 1;
    }

    private static int setCurrentSpawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        // Use the respawn handler to set current location as spawn
        PlayerRespawnHandler.setCurrentLocationAsSpawn(player);
        
        // Get current dimension for feedback
        Identifier currentWorld = player.getWorld().getRegistryKey().getValue();
        Identifier planet = currentWorld.equals(Identifier.of("minecraft", "overworld")) ? 
            Identifier.of("minecraft", "planet") : currentWorld;
        
        source.sendFeedback(() -> Text.literal("§aSpawn point set for planet: " + planet + " at " + player.getBlockPos().toShortString()), true);
        return 1;
    }

    private static int designatePlanet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Identifier planetId = IdentifierArgumentType.getIdentifier(context, "planet");
        
        // Set as designated planet
        PlayerPlanetData.setDesignatedPlanet(player.getUuid(), planetId);
        
        source.sendFeedback(() -> Text.literal("§aDesignated planet set to: " + planetId), true);
        return 1;
    }
}
