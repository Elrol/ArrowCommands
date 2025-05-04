package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import dev.elrol.arrow.menus._MenuBase;
import dev.elrol.arrow.registries.ModPlayerDataRegistry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DaycareCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        root = dispatcher.register(literal("daycare").requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "daycare").asBoolean();
                    }
                    return true;
                })
                .executes(context -> {
                    ServerPlayerEntity player = getPlayer(context);
                    if(player != null) {
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                        PlayerDataCore coreData = data.get(new PlayerDataCore());
                        PlayerDataCommands commandData = data.get(new PlayerDataCommands());

                        coreData.menuHistory.clear();

                        _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("daycare", player);
                        if(menu == null) {
                            ArrowCommands.LOGGER.error("Menu for Daycare failed to create");
                            return 0;
                        }
                        data.put(coreData);
                        data.put(commandData);
                        menu.open();
                    } else {
                        context.getSource().sendMessage(ModTranslations.err("not_player"));
                    }
                    return 1;
                })
        );
    }
}
