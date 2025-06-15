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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.zamecki.astralis.Astralis;
import org.zamecki.astralis.dimension.PlanetDimensions;

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
                        builder.suggest("minecraft:overworld");
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

        RegistryKey<World> worldKey;
        
        // Handle special cases
        if (planetId.equals(Identifier.of("minecraft", "overworld"))) {
            worldKey = World.OVERWORLD;
        } else if (planetId.getNamespace().equals(MOD_ID)) {
            String planetName = planetId.getPath();
            worldKey = PlanetDimensions.getPlanetDimensions().get(planetName);
            
            if (worldKey == null) {
                source.sendError(Text.literal("Unknown planet: " + planetName));
                return 0;
            }
        } else {
            source.sendError(Text.literal("Invalid planet. Must be in namespace '" + MOD_ID + "' or 'minecraft:overworld'"));
            return 0;
        }

        ServerWorld targetWorld = source.getServer().getWorld(worldKey);
        if (targetWorld == null) {
            source.sendError(Text.literal("Planet " + planetId.getPath() + " is not loaded or does not exist"));
            return 0;
        }

        // Default spawn coordinates
        int x = 0;
        int z = 0;
        int y = 100;

        // Ensure the chunk is loaded using the proper Minecraft 1.21.5 approach
        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        
        // Force chunk loading synchronously
        targetWorld.getChunk(chunkPos.x, chunkPos.z);
        
        // Find a safe height to teleport to
        y = targetWorld.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z) + 1;
        if (y <= targetWorld.getBottomY()) {
            y = targetWorld.getSeaLevel() + 10; // Fallback to sea level + 10
        }

        // Ensure the position is safe
        BlockPos teleportPos = new BlockPos(x, y, z);
        
        // Teleport the player
        player.teleport(targetWorld,
                teleportPos.getX() + 0.5, 
                teleportPos.getY(), 
                teleportPos.getZ() + 0.5,
                Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z),
                player.getYaw(),
                player.getPitch(),
                true);

        source.sendFeedback(() -> Text.literal("Teleported to planet: " + planetId.toString()), true);
        Astralis.LOGGER.info("Player {} teleported to planet: {}", player.getName().getString(), planetId.toString());
        return 1;
    }
}
