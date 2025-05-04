package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.menus._MenuBase;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class _CommandMenuBase extends _MenuBase {

    protected final PlayerDataCommands commandData;

    public <T extends ScreenHandler> _CommandMenuBase(ServerPlayerEntity player, ScreenHandlerType<T> type) {
        super(player, type);
        commandData = data.get(new PlayerDataCommands());
    }

}
