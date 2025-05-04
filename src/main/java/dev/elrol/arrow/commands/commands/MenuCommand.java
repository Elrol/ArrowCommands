package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IMenuRegistry;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.menus._MenuBase;
import dev.elrol.arrow.registries.ModPlayerDataRegistry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class MenuCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("menu")
                .executes((context) -> {
                    ServerPlayerEntity player = getPlayer(context);
                    if(player != null) {
                        _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("main", player);
                        if(menu == null) {
                            ArrowCommands.LOGGER.error("Menu for Main Menu failed to create");
                            return 0;
                        }
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
                        PlayerDataCore coreData = data.get(new PlayerDataCore());
                        coreData.menuHistory.clear();
                        data.put(coreData);
                        menu.open();
                    } else {
                        context.getSource().sendMessage(ModTranslations.err("not_player"));
                    }
                    return 1;
                }));
    }
}