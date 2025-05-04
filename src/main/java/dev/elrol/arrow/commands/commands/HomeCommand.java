package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.HomeSuggestionProvider;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HomeCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("home")
                .executes(this::noArgs).requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "home").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(new HomeSuggestionProvider())
                        .executes(this::oneArgs).requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "home").asBoolean();
                            }
                            return true;
                        })));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            PlayerDataCommands commandData = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid()).get(new PlayerDataCommands());
            int maxHomes = PermUtils.getMetaData(player).getMetaValue("homes", Integer::parseInt).orElse(1);

            int currentHomes = commandData.homes.size();

            if(currentHomes > maxHomes) {
                context.getSource().sendError(ModTranslations.err("too_many_homes", currentHomes - maxHomes, currentHomes));
                return 0;
            }

            if(commandData.goHome("home", player)) {
                context.getSource().sendMessage(ModTranslations.msg("home_welcome"));
            } else {
                context.getSource().sendMessage(ModTranslations.err("home_missing"));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
    private int oneArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String name = StringArgumentType.getString(context, "name");
        if(player != null) {
            PlayerDataCommands commandData = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid()).get(new PlayerDataCommands());

            int maxHomes = PermUtils.getMetaData(player).getMetaValue("homes", Integer::parseInt).orElse(1);
            int currentHomes = commandData.homes.size();

            if(currentHomes > maxHomes) {
                context.getSource().sendError(ModTranslations.err("too_many_homes", currentHomes - maxHomes, currentHomes));
                return 0;
            }

            if(commandData.goHome(name, player)) {
                context.getSource().sendMessage(ModTranslations.msg("home_welcome_1", name));
            } else {
                context.getSource().sendMessage(ModTranslations.err("home_missing_1", name));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}