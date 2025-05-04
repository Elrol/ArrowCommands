package dev.elrol.arrow.commands.menus.shops;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.data.ShoppingData;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.menus._MenuBase;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class ItemSelectMenu extends _CommandMenuBase {

    ShoppingData shoppingData;
    GuiElementBuilder shopItemElement;

    public ItemSelectMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
        shoppingData = commandData.shoppingData;
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();
        ListingData listingData = shoppingData.currentCart;
        if(!listingData.isEmpty()) {
            int current = listingData.getUnits();
            shopItemElement = MenuUtils.itemStack(listingData.getItem(), listingData.getItem().getName()).addLoreLine(ModTranslations.translate("arrow.menu.shop.select.amount").formatted( Formatting.GREEN).append(ModTranslations.literal (" " + current).formatted(Formatting.GRAY)));

            setSlot(18, changeAmount(CommandsMenuItems.RED_BUTTON_4, listingData, -64));
            setSlot(19, changeAmount(CommandsMenuItems.RED_BUTTON_3, listingData, -16));
            setSlot(20, changeAmount(CommandsMenuItems.RED_BUTTON_2, listingData, -8));
            setSlot(21, changeAmount(CommandsMenuItems.RED_BUTTON_1, listingData, -1));

            setSlot(22, shopItemElement);

            setSlot(23, changeAmount(CommandsMenuItems.LIME_BUTTON_1, listingData, 1));
            setSlot(24, changeAmount(CommandsMenuItems.LIME_BUTTON_2, listingData, 8));
            setSlot(25, changeAmount(CommandsMenuItems.LIME_BUTTON_3, listingData, 16));
            setSlot(26, changeAmount(CommandsMenuItems.LIME_BUTTON_4, listingData, 64));

            setSlot(29, cancel(CommandsMenuItems.RED_BUTTON_LEFT));
            setSlot(30, cancel(CommandsMenuItems.RED_BUTTON_RIGHT));

            setSlot(32, confirm(CommandsMenuItems.LIME_BUTTON_LEFT, CommandsMenuItems.GRAY_BUTTON_LEFT, listingData));
            setSlot(33, confirm(CommandsMenuItems.LIME_BUTTON_RIGHT, CommandsMenuItems.GRAY_BUTTON_RIGHT, listingData));
        }
    }

    public GuiElementBuilder changeAmount(Item item, ListingData listingData, int amount) {
        boolean isPositive = amount > 0;
        Formatting format = (isPositive ? Formatting.GREEN : Formatting.RED);

        GuiElementBuilder element = MenuUtils.item(item, 1, ModTranslations.literal((isPositive ? "+" : "") + amount).formatted(format, Formatting.BOLD)).setCallback(()->{
            listingData.changeUnits(amount);
            commandData.shoppingData.currentCart = listingData;
            data.put(commandData);
            click();
            drawMenu();
        });

        element.addLoreLine(
                Text.literal(listingData.getUnits() + " ─> " + Math.max(listingData.getUnits() + amount, 0))
                        .setStyle(Style.EMPTY.withItalic(false).withFormatting(format))
        );

        return element;
    }

    public GuiElementBuilder cancel(Item item) {
        return MenuUtils.item(item, 1, ModTranslations.translate("arrow.menu.shop.select.cancel").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback(() -> {
                    click();
                    commandData.shoppingData.cancelCurrentCart();
                    Objects.requireNonNull(ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_shop", player)).open();
                });
    }

    public GuiElementBuilder confirm(Item item, Item disabled, ListingData listingData) {
        int units = commandData.shoppingData.currentCart.getUnits();
        boolean flag = units > 0;
        return MenuUtils.item((flag ? item : disabled), 1, ModTranslations.translate("arrow.menu.shop.select.add").formatted(Formatting.BOLD, (flag ? Formatting.GREEN : Formatting.DARK_GRAY)))
                .addLoreLine(ModTranslations.translate("arrow.menu.shop.select.amount").append(Text.literal(" " + units)))
                .addLoreLine(ModTranslations.literal(listingData.getPriceString()).formatted((flag ? Formatting.DARK_GREEN : Formatting.DARK_GRAY)))
                .setCallback(() -> {
                    if(!flag) return;
                    commandData.shoppingData.confirmCurrentCart();
                    _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_shop", player);
                    if(menu == null) {
                        if(ArrowCore.CONFIG.isDebug)
                            ArrowCommands.LOGGER.error("Item Shop Menu was null when being created");
                        return;
                    }
                    data.put(commandData);
                    click();
                    menu.open();
                });
    }

    @Override
    public int getMenuID() {
        return 10;
    }

    @Override
    public char getMenuUnicode() {
        return '≊';
    }

    @Override
    public String getMenuName() {
        return "item_select";
    }
}
