package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class MainMenu extends _CommandMenuBase {

    public MainMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        setSlot(21, MenuUtils.item(CommandsMenuItems.DAYCARE_BUTTON, 1, "daycare").setCallback(() -> {
            click();
            commandData.daycareData.slot1 = -1;
            commandData.daycareData.slot2 = -1;
            navigateToMenu("daycare");
        }));

        if(PermUtils.hasPerm(player, "arrow.command", "shop").asBoolean())
            setSlot(22, MenuUtils.item(CommandsMenuItems.SHOP_BUTTON, 1, "shop").setCallback(() -> {
                click();
                navigateToMenu("shop");
            }));

        if(PermUtils.hasPerm(player, "arrow.command", "customizer").asBoolean()) {
            setSlot(23, MenuUtils.item(CommandsMenuItems.CUSTOMIZER_BUTTON, 1, "customizer").setCallback(() -> {
                click();
                navigateToMenu("customizer");
            }));
        }
    }

    @Override
    public int getMenuID() {
        return 100;
    }

    @Override
    public char getMenuUnicode() {
        return '≀';
    }

    @Override
    public String getMenuName() {
        return "main";
    }
}
