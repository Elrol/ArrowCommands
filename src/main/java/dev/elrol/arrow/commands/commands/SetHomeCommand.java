package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IPlayerDataRegistry;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.HomeSuggestionProvider;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SetHomeCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(CommandManager.literal("sethome")
                .executes(this::noArgs).requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "home").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(new HomeSuggestionProvider())
                        .executes(this::oneArg).requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "home").asBoolean();
                            }
                            return true;
                        })));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            try {
                IPlayerDataRegistry dataRegistry = ArrowCore.INSTANCE.getPlayerDataRegistry();
                PlayerData data = dataRegistry.getPlayerData(player);
                PlayerDataCommands commandData = data.get(new PlayerDataCommands());

                int maxHomes = PermUtils.getMetaData(player).getMetaValue("homes", Integer::parseInt).orElse(1);
                int currentHomes = commandData.homes.size();

                if(currentHomes >= maxHomes && !commandData.homes.containsKey("home")) {
                    context.getSource().sendError(ModTranslations.err("no_more_homes", currentHomes));
                    return 0;
                }

                commandData.setHome("home", player);
                data.put(commandData);
                context.getSource().sendMessage(ModTranslations.msg("home_set"));
            } catch (RuntimeException e) {
                ArrowCommands.LOGGER.error(e.getLocalizedMessage());
            }
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
        }
        return 1;
    }

    private int oneArg(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String name = StringArgumentType.getString(context, "name");
        if(player != null) {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            int maxHomes = PermUtils.getMetaData(player).getMetaValue("homes", Integer::parseInt).orElse(1);
            int currentHomes = commandData.homes.size();

            if(currentHomes >= maxHomes && !commandData.homes.containsKey(name)) {
                context.getSource().sendError(ModTranslations.err("no_more_homes", currentHomes));
                return 0;
            }
            commandData.setHome(name, player);
            data.put(commandData);
            context.getSource().sendMessage(ModTranslations.msg("home_set_1", name));
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}
