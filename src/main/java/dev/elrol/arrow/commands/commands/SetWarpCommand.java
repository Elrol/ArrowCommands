package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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

public class SetWarpCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("setwarp")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin", "setwarp").asBoolean();
                            }
                            return true;
                        })
                        .executes(this::oneArgs).requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin", "setwarp").asBoolean();
                            }
                            return true;
                        })));
    }

    private int oneArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String name = StringArgumentType.getString(context, "name");
        if(player != null) {
            IServerDataRegistry registry = ArrowCore.INSTANCE.getServerDataRegistry();
            ServerDataCommands commandData = registry.get(ServerDataCommands.class);
            commandData.addWarp(player, name);
            registry.put(commandData, true);

        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
        }
        return 1;
    }
}
