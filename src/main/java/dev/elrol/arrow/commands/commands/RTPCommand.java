package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.CommandConfig;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.luckperms.api.node.Node;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.time.Duration;
import java.util.Random;

public class RTPCommand extends _CommandBase {

    BlockPos pos;
    BlockPos pos1;
    BlockPos pos2;

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("rtp").executes(this::noArgs).requires((source)->{
            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "rtp").asBoolean();
            }
            return true;
        }));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            ServerWorld world = player.getServerWorld();
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCore coreData = data.get(new PlayerDataCore());

            int count = 0;
            do {
                findRandomLocation(world);
                count++;
                if(count >= 20) {
                    player.sendMessage(ModTranslations.err("rtp_failed"));
                    return 0;
                }
            } while (!world.isTopSolid(pos, player));

            ExactLocation location = new ExactLocation(world, (double)pos.getX() + 0.5d, (double)pos1.getY() + 0.5d, (double)pos.getZ() + 0.5d, 0.0f, 0.0f);
            coreData.logTeleport(player);
            location.teleport(player);
            player.sendMessage(ModTranslations.msg("rtp_success", count));
            PermUtils.getUser(player).data().add(
                    Node.builder("arrow.command.rtp")
                            .value(false)
                            .expiry(Duration.ofMinutes(5))
                            .build()
            );
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }

    private void findRandomLocation(ServerWorld world) {
        pos1 = getRandomLocation(world);
        pos = pos1.down();
        pos2 = pos1.up();
    }

    private BlockPos getRandomLocation(ServerWorld world) {
        CommandConfig config = ArrowCommands.CONFIG;
        int min = config.rtpMin;
        int max = config.rtpMax;

        Random rand = new Random();
        int x = rand.nextInt(min, max) * (rand.nextBoolean() ? 1 : -1);
        int z = rand.nextInt(min, max) * (rand.nextBoolean() ? 1 : -1);
        world.getChunk(new BlockPos(x, 64, z));

        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

        return new BlockPos(x,y,z);
    }
}
