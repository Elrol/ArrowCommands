package dev.elrol.arrow.commands.menus.shops;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.data.ShoppingData;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class ItemSelectMenu extends _CommandMenuBase {

    ShoppingData shoppingData;
    GuiElementBuilder shopItemElement;
    ListingData listingData;

    ItemSelectFunction confirmFunction;
    Runnable cancelFunction;

    public ItemSelectMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
        shoppingData = commandData.shoppingData;
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        if(!listingData.isEmpty()) {
            int current = listingData.getUnits();

            shopItemElement = MenuUtils.itemStack(
                    listingData.getItem(),
                    listingData.getItem().getName());
            shopItemElement.addLoreLine(ModTranslations.translate("arrow.menu.shop.select.amount").formatted( Formatting.GREEN).append(ModTranslations.literal (" " + current).formatted(Formatting.GRAY)));
            shopItemElement.addLoreLine(ModTranslations.translate("arrow.menu.shop.select.cost").formatted(Formatting.GREEN).append(ModTranslations.literal(" " + listingData.getPriceString()).formatted(Formatting.GRAY)));

            setSlot(18, changeAmount(CommandsMenuItems.RED_BUTTON_4, -64));
            setSlot(19, changeAmount(CommandsMenuItems.RED_BUTTON_3, -16));
            setSlot(20, changeAmount(CommandsMenuItems.RED_BUTTON_2, -8));
            setSlot(21, changeAmount(CommandsMenuItems.RED_BUTTON_1, -1));

            setSlot(22, shopItemElement);

            setSlot(23, changeAmount(CommandsMenuItems.LIME_BUTTON_1, 1));
            setSlot(24, changeAmount(CommandsMenuItems.LIME_BUTTON_2, 8));
            setSlot(25, changeAmount(CommandsMenuItems.LIME_BUTTON_3, 16));
            setSlot(26, changeAmount(CommandsMenuItems.LIME_BUTTON_4, 64));

            setSlot(29, cancel(CommandsMenuItems.RED_BUTTON_LEFT));
            setSlot(30, cancel(CommandsMenuItems.RED_BUTTON_RIGHT));

            setSlot(32, confirm(CommandsMenuItems.LIME_BUTTON_LEFT, CommandsMenuItems.GRAY_BUTTON_LEFT));
            setSlot(33, confirm(CommandsMenuItems.LIME_BUTTON_RIGHT, CommandsMenuItems.GRAY_BUTTON_RIGHT));
        }
    }

    @Override
    public void open() {
        ArrowCommands.LOGGER.error("Item Select Menu was attempted to be opened without having a listing set.");
    }

    public void open(ListingData listingData, boolean clearHistory, ItemSelectFunction selectFunction, Runnable cancelFunction) {
        this.listingData = listingData;
        this.confirmFunction = selectFunction;
        this.cancelFunction = cancelFunction;
        super.open(clearHistory);
    }

    public GuiElementBuilder changeAmount(Item item, int amount) {
        boolean isPositive = amount > 0;
        Formatting format = (isPositive ? Formatting.GREEN : Formatting.RED);
        int targetAmount = Math.max(listingData.getUnits() + amount, 0);
        IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        GuiElementBuilder element = MenuUtils.item(item, 1, ModTranslations.literal((isPositive ? "+" : "") + amount).formatted(format, Formatting.BOLD)).setCallback(()->{
            listingData.changeUnits(amount);
            click();
            drawMenu();
        });

        element.addLoreLine(ModTranslations.literal(listingData.getUnits() + " ─> " + targetAmount).formatted(format));
        element.addLoreLine(ModTranslations.literal(listingData.getPriceString() + " -> " + econRegistry.formatAmount(listingData.getPricePerUnit() * targetAmount)).formatted(format));

        return element;
    }

    public GuiElementBuilder cancel(Item item) {
        return MenuUtils.item(item, 1, ModTranslations.translate("arrow.menu.shop.select.cancel").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback(() -> {
                    if(cancelFunction != null) {
                        click();
                        cancelFunction.run();
                    }
                });
    }

    public GuiElementBuilder confirm(Item item, Item disabled) {
        int units = commandData.shoppingData.currentCart.getUnits();
        boolean flag = units > 0;
        return MenuUtils.item((flag ? item : disabled), 1, ModTranslations.translate("arrow.menu.shop.select.add").formatted(Formatting.BOLD, (flag ? Formatting.GREEN : Formatting.DARK_GRAY)))
                .addLoreLine(ModTranslations.translate("arrow.menu.shop.select.amount").formatted(Formatting.GREEN).append(ModTranslations.literal (" " + units).formatted(Formatting.GRAY)))
                .addLoreLine(ModTranslations.translate("arrow.menu.shop.select.cost").formatted(Formatting.GREEN).append(ModTranslations.literal(" " + listingData.getPriceString()).formatted(Formatting.GRAY)))
                .setCallback(() -> {
                    if(!flag) return;
                    if(confirmFunction != null) {
                        click();
                        confirmFunction.select(listingData);
                    }
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
    public @NotNull String getMenuName() {
        return "item_select";
    }

    @FunctionalInterface
    public interface ItemSelectFunction {
        void select(ListingData listingData);
    }
}
