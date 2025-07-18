package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CreateShopCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("createshop")
                .executes(this::noArgs)
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "createshop").asBoolean();
                    }
                    return true;
                }));
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        if(player != null) {
            ArrowCore.INSTANCE.getMenuRegistry().createMenu("shop_setup", player).open(true);
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}