package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.libs.CommandsConstants;
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
            //Check to see how many shops a player can have
            int maxShops = PermUtils.getMetaData(player).getMetaValue(CommandsConstants.MetaKeys.MAX_PLAYER_SHOPS, Integer::parseInt).orElse(0);
            int addShops = PermUtils.getMetaData(player).getMetaValue(CommandsConstants.MetaKeys.ADD_PLAYER_SHOPS, Integer::parseInt).orElse(0);
            int curShops = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player).get(new PlayerDataCommands()).playerShopData.getShopCount();

            if(curShops < (maxShops + addShops)) {
                ArrowCore.INSTANCE.getMenuRegistry().createMenu("shop_setup", player).open(true);
            } else {
                player.sendMessage(ModTranslations.err("max_shops"));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}