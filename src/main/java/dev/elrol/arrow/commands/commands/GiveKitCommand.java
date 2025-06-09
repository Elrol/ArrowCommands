package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.KitSuggestionProvider;
import dev.elrol.arrow.commands.data.KitData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.registries.KitRegistry;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.LocalDateTime;
import java.util.Collection;

public class GiveKitCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("givekit").requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "givekit").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("kit", StringArgumentType.string())
                            .suggests(new KitSuggestionProvider(true))
                            .executes(this::twoArgs)))
        );
    }

    private int twoArgs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        String ID = StringArgumentType.getString(context, "kit");

        KitData kit = KitRegistry.get(ID);

        if(kit == null) return 0;

        players.forEach(player -> {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            kit.giveKit(player);
            if(kit.cooldown > 0 || kit.oneTimeUse) {
                commandData.kitTimeStamps.put(ID, LocalDateTime.now());
                data.put(commandData);
            }
        });
        return 1;
    }
}