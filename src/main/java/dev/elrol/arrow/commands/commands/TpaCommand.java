package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.libs.CommandUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TpaCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("tpa")
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "tpa").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(this::oneArgs)));
    }

    private int oneArgs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = getPlayer(context);
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        if(player != null) {
            if(target != null) {
                CommandUtils.requestTeleport(player, target, false);
            } else {
                player.sendMessage(ModTranslations.err("player_missing"));
            }
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
        }
        return 1;
    }
}