package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.menus._PageMenuBase;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class _CommandPageMenuBase extends _PageMenuBase {

    protected PlayerDataCommands commandData;

    public _CommandPageMenuBase(ServerPlayerEntity player) {
        super(player);
        commandData = data.get(new PlayerDataCommands());
    }
}
