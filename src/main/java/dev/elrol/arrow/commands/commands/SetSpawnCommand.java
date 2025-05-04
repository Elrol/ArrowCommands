package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IServerDataRegistry;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.ServerDataCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SetSpawnCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("setspawn").requires((source)->{
            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin", "setspawn").asBoolean();
            }
            return true;
        }).executes(this::noArgs));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            IServerDataRegistry serverData = ArrowCore.INSTANCE.getServerDataRegistry();
            ServerDataCommands serverDataCommands = serverData.get(ServerDataCommands.class);
            serverDataCommands.setSpawnLocation(player);
            serverData.put(serverDataCommands, true);

            context.getSource().sendMessage(ModTranslations.msg("spawn_set"));
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
        }
        return 1;
    }
}
