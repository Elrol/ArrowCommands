package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.elrol.arrow.ArrowCore;
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
import net.minecraft.text.Text;

public class SettingsCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("settings").executes((context) -> {
            ServerPlayerEntity player = getPlayer(context);
            if(player != null) {
                _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("settings", player);
                if(menu == null) {
                    ArrowCommands.LOGGER.error("Menu for Settings failed to create");
                    return 0;
                }
                PlayerDataCore settings = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid()).get(new PlayerDataCore());
                settings.menuHistory.clear();
                menu.open();
            } else {
                context.getSource().sendError(ModTranslations.err("not_player"));
            }
            return 1;
        }));
    }
}