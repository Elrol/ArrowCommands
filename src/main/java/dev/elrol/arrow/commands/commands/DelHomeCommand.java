package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.HomeSuggestionProvider;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import dev.elrol.arrow.registries.ModPlayerDataRegistry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DelHomeCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(literal("delhome")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(new HomeSuggestionProvider())
                        .executes(this::oneArg).requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "delhome").asBoolean();
                            }
                            return true;
                        })));
    }

    private int oneArg(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String name = StringArgumentType.getString(context, "name");
        if(player != null) {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            if(commandData.delHome(name)) {
                context.getSource().sendMessage(ModTranslations.msg("home_deleted", name));
                data.put(commandData);
            } else {
                context.getSource().sendMessage(ModTranslations.err("home_delete_missing", name));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}
