package dev.elrol.arrow.commands.menus;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class SettingsMenu extends _CommandMenuBase {
    public SettingsMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);

    }

    @Override
    protected void drawMenu() {
        super.drawMenu();
    }

    @Override
    public int getMenuID() {
        return 1;
    }

    @Override
    public char getMenuUnicode() {
        return '≁';
    }

    @Override
    public String getMenuName() {
        return "settings";
    }
}
