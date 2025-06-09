package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.HomeSuggestionProvider;
import dev.elrol.arrow.commands.commands.suggestions.PlayerSuggestionProvider;
import dev.elrol.arrow.commands.data.OnTimeData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.libs.DateTimeUtils;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class OnTimeCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("ontime")
                .executes(this::noArgs)
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command.ontime", "self").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("player", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.ontime", "other").asBoolean();
                            }
                            return true;
                        })
                        .executes(this::oneArg)));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            commandData.onTimeData.logTime();
            data.put(commandData);

            OnTimeData onTime = commandData.onTimeData;

            player.sendMessage(ModTranslations.info("own_on_time").formatted(Formatting.GRAY, Formatting.BOLD));
            player.sendMessage(ModTranslations.info("time_today").formatted(Formatting.RED).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getDayOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_week").formatted(Formatting.YELLOW).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getWeekOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_month").formatted(Formatting.GREEN).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getMonthOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_total").formatted(Formatting.AQUA).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getTotalOnTime())).formatted(Formatting.WHITE)));

        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }

    private int oneArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

            if(player.server.getPlayerManager().getPlayer(data.uuid) != null) {
                commandData.onTimeData.logTime();
                data.put(commandData);
            }

            OnTimeData onTime = commandData.onTimeData;



            player.sendMessage(ModTranslations.info("other_on_time", targetName).formatted(Formatting.GRAY, Formatting.BOLD));
            player.sendMessage(ModTranslations.info("time_today").formatted(Formatting.RED).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getDayOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_week").formatted(Formatting.YELLOW).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getWeekOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_month").formatted(Formatting.GREEN).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getMonthOnTime())).formatted(Formatting.WHITE)));
            player.sendMessage(ModTranslations.info("time_total").formatted(Formatting.AQUA).append(ModTranslations.literal(DateTimeUtils.formatDateTime(onTime.getTotalOnTime())).formatted(Formatting.WHITE)));

        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}