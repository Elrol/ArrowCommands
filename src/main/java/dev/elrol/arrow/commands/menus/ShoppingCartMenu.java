package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.util.*;

public class ShoppingCartMenu extends _CommandPageMenuBase {

    public ShoppingCartMenu(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        Map<String, ListingData> cart = new HashMap<>();
        List<ListingData> shoppingCart = commandData.shoppingData.shoppingCart;

        for(int i = 0; i < shoppingCart.size(); i++) {
            cart.put(String.valueOf(i), shoppingCart.get(i));
        }

        drawItems(cart);

        setSlot(28, cancel(CommandsMenuItems.RED_BUTTON_LEFT, CommandsMenuItems.GRAY_BUTTON_LEFT));
        setSlot(29, cancel(CommandsMenuItems.RED_BUTTON_MIDDLE, CommandsMenuItems.GRAY_BUTTON_MIDDLE));
        setSlot(30, cancel(CommandsMenuItems.RED_BUTTON_RIGHT, CommandsMenuItems.GRAY_BUTTON_RIGHT));

        setSlot(32, confirm(CommandsMenuItems.LIME_BUTTON_LEFT, CommandsMenuItems.GRAY_BUTTON_LEFT));
        setSlot(33, confirm(CommandsMenuItems.LIME_BUTTON_MIDDLE, CommandsMenuItems.GRAY_BUTTON_MIDDLE));
        setSlot(34, confirm(CommandsMenuItems.LIME_BUTTON_RIGHT, CommandsMenuItems.GRAY_BUTTON_RIGHT));
    }

    private GuiElementBuilder confirm(Item button, Item disabledButton){
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        double total = commandData.shoppingData.getTotalPrice();
        BigDecimal balance = economyRegistry.getBal(player.getUuid());
        boolean canConfirm = !commandData.shoppingData.shoppingCart.isEmpty();
        boolean flag = (balance.compareTo(BigDecimal.valueOf(total)) >= 0 || total <= 0) && canConfirm ;
        Formatting loreFormat = flag ? Formatting.DARK_GREEN : Formatting.RED;

        String balString = "  " + economyRegistry.formatAmount(balance);
        String totalString = "- " + economyRegistry.formatAmount(total);

        List<Text> lore = new ArrayList<>();
        lore.add(ModTranslations.literal(balString).formatted(loreFormat));
        lore.add(ModTranslations.literal(totalString).formatted(loreFormat));
        lore.add(ModTranslations.literal("─".repeat(Math.max(balString.length(), totalString.length()))).formatted(Formatting.GRAY));
        lore.add(ModTranslations.literal("  " + economyRegistry.formatAmount(balance.subtract(BigDecimal.valueOf(total)))).formatted(loreFormat));

        return MenuUtils.item(flag ? button : disabledButton, 1, ModTranslations.translate("arrow.menu.shop.cart.confirm").formatted (flag ? Formatting.GREEN : Formatting.DARK_GRAY)).setCallback(() -> {
            if(!flag) return;
            click();
            commandData.shoppingData.finalizePurchase(player);
            data.put(commandData);
            returnToMenu();
        }).setLore(lore);
    }

    private GuiElementBuilder cancel(Item button, Item disabledButton){
        boolean isCartEmpty = commandData.shoppingData.shoppingCart.isEmpty();
        return MenuUtils.item(isCartEmpty ? disabledButton : button, 1, "clear_cart").setCallback(() -> {
            if(isCartEmpty) return;
            click();
            commandData.shoppingData.shoppingCart.clear();
            data.put(commandData);
            Objects.requireNonNull(ArrowCore.INSTANCE.getMenuRegistry().createMenu(getMenuName(), player)).open();
        });
    }

    @Override
    public int getMenuID() {
        return 11;
    }

    @Override
    public char getMenuUnicode() {
        return '≋';
    }

    @Override
    public String getMenuName() {
        return "shopping_cart";
    }

    @Override
    protected <T> GuiElementBuilder createElement(String key, Map<String, T> map) {
        ListingData listingData = (ListingData) map.get(key);
        ItemStack listingItem = listingData.getItem();
        GuiElementBuilder in = MenuUtils.itemStack(listingItem, listingItem.getName());
        in.addLoreLine(ModTranslations.translate("arrow.menu.shop.cart.amount").append(Text.literal(" " + listingData.getUnits()).setStyle(Style.EMPTY.withItalic(false)).formatted(Formatting.WHITE)));
        in.addLoreLine(Text.literal(listingData.getPriceString()).setStyle(Style.EMPTY.withItalic(false)).formatted(Formatting.GREEN));

        in.setCallback(() -> {

        });
        return in;
    }

    @Override
    public int getLastPage() {
        return Math.floorDiv(commandData.shoppingData.shoppingCart.size(),15);
    }
}
