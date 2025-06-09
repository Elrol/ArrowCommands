package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.PlayerSuggestionProvider;
import dev.elrol.arrow.commands.data.OnTimeData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.libs.DateTimeUtils;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.time.format.DateTimeFormatter;

public class SeenCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("seen")
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "seen").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("player", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(this::oneArg)));
    }

    private int oneArg(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String targetName = StringArgumentType.getString(context, "player");


        if(player != null) {
            PlayerData data = null;

            for(PlayerData playerData : ArrowCore.INSTANCE.getPlayerDataRegistry().getLoadedData().values()) {
                PlayerDataCore coreData = playerData.get(new PlayerDataCore());
                if(coreData.username.getString().equalsIgnoreCase(targetName)) {
                    data = playerData;
                    break;
                }
            }

            if (data == null) {
                player.sendMessage(ModTranslations.err("player_missing"));
                return 0;
            }

            PlayerDataCore coreData = data.get(new PlayerDataCore());
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());

            String online;
            if(player.server.getPlayerManager().getPlayer(data.uuid) != null) {
                commandData.onTimeData.logTime();
                data.put(commandData);

                online = "Online";
            } else if(commandData.onTimeData.getLastOnline() == null) {
                online = "Offline";
            } else {
                online = commandData.onTimeData.getLastOnline().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
            }

            player.sendMessage(
                    ModTranslations.info("last_seen", coreData.username.getString()).formatted(Formatting.GREEN)
                            .append(ModTranslations.literal(online).formatted(Formatting.GRAY)));

        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}