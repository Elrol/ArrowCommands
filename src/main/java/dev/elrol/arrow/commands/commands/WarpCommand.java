package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.WarpSuggestionProvider;
import dev.elrol.arrow.commands.data.ServerDataCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WarpCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("warp")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(new WarpSuggestionProvider())
                        .executes(this::oneArgs)));
    }

    private int oneArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String warpName = StringArgumentType.getString(context, "name");
        ServerDataCommands data = ArrowCore.INSTANCE.getServerDataRegistry().get(ServerDataCommands.class);

        if(player != null) {
            if(!PermUtils.hasPerm(player, "arrow.warp", warpName).asBoolean()) {
                player.sendMessage(ModTranslations.err("warp_missing", warpName));
                return 0;
            }
            if(data.teleportToWarp(player, warpName)) {
                player.sendMessage(ModTranslations.msg("warped").append(Text.literal(warpName).formatted(Formatting.AQUA)));
            } else {
                context.getSource().sendMessage(ModTranslations.err("warp_missing", warpName));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}