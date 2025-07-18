package dev.elrol.arrow.commands.menus.createshop.item;

import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.libs.MenuUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ItemShopSetupMenu extends _CommandMenuBase {

    public ItemShopSetupMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        setSlot(20, MenuUtils.item(Items.APPLE, 1, Text.literal("Item Shop")).setCallback(() -> {

        }));

    }

    @Override
    public int getMenuID() {
        return 112;
    }

    @Override
    public char getMenuUnicode() {
        return '⎣';
    }

    @Override
    public @NotNull String getMenuName() {
        return "item_shop_setup";
    }
}
