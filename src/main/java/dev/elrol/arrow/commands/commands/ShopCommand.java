package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import dev.elrol.arrow.menus._MenuBase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShopCommand extends _CommandBase {


    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("shop")
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "shop").asBoolean();
                    }
                    return true;
                })
                .executes(context -> {
                    ServerPlayerEntity player = getPlayer(context);
                    if(player != null) {
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
                        PlayerDataCore coreData = data.get(new PlayerDataCore());
                        data.put(coreData);
                        coreData.menuHistory.clear();

                        _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("shop", player);
                        if(menu == null) {
                            ArrowCommands.LOGGER.error("Menu for Shop failed to create");
                            return 0;
                        }
                        menu.open();
                    } else {
                        context.getSource().sendError(ModTranslations.err("not_player"));
                    }
                    return 1;
                })
        );
    }
}
