package dev.elrol.arrow.commands.menus.shops;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.CommandConfig;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.menus._CommandPageMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class ItemShopMenu extends _CommandPageMenuBase {
    String shopName;
    CommandConfig.ShopItems shopItems;
    Map<String, CommandConfig.ShopItem> items = new HashMap<>();

    public ItemShopMenu(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public void open() {
        super.open();
        ArrowCommands.LOGGER.error("Page Number: {}", page);
    }

    @Override
    protected void drawMenu() {
        shopName = commandData.shoppingData.shop;
        if(shopName.isEmpty()) {
            ArrowCommands.LOGGER.error("Shop Name was missing");
            return;
        }

        shopItems = ArrowCommands.CONFIG.shopSettings.shops.get(shopName);

        if(ArrowCore.CONFIG.isDebug) {
            ArrowCommands.LOGGER.warn("Shop Name: {}", shopName);
        }

        if(shopItems.itemShop.isEmpty()) {
            ArrowCommands.LOGGER.error("Item Shop {} has no items to sell", shopName);
        }

        for(int i = 0; i < shopItems.itemShop.size(); i++) {
            CommandConfig.ShopItem item = shopItems.itemShop.get(i);
            String perm = "arrow.shops." + shopName;

            if(ArrowCore.CONFIG.isDebug) {
                ArrowCommands.LOGGER.warn("Shop Name: {}.{}", shopName, i);
            }

            if(PermUtils.hasPerm(player, perm, String.valueOf(i)).asBoolean()) {
                items.put(String.valueOf(i), item);
            } else {
                if(ArrowCore.CONFIG.isDebug) {
                    ArrowCommands.LOGGER.warn("Missing Permission: {}.{}", shopName, i);
                }
            }
        }

        super.drawMenu();

        drawItems(items);

        setSlot(0, MenuUtils.item(CommandsMenuItems.CART_BUTTON, 1, ModTranslations.translate("arrow.menu.shop.cart").formatted(Formatting.GREEN, Formatting.BOLD)).setCallback(() -> {
            click();
            navigateToMenu("shopping_cart");
        }));
    }

    @Override
    public int getMenuID() {
        return 9;
    }

    @Override
    public char getMenuUnicode() {
        return '≉';
    }

    @Override
    public String getMenuName() {
        return "item_shop";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> GuiElementBuilder createElement(String key, Map<String, T> map) {
        CommandConfig.ShopItem shopItem = ((Map<String, CommandConfig.ShopItem>) map).get(key);
        ItemStack item = shopItem.item;
        GuiElementBuilder in = MenuUtils.itemStack(item.copyWithCount(1), item.getName());
        //in.setCustomModelData((shopItems.shopID * 1000000) + (getMenuID() * 1000) + Integer.parseInt(key));

        in.addLoreLine(ModTranslations.literal(ArrowCore.INSTANCE.getEconomyRegistry().formatAmount(shopItem.cost)).formatted(Formatting.GREEN));
        return in.setCallback(() -> {
            click();
            commandData.shoppingData.currentCart = new ListingData(item, shopItem.cost, 0);
            data.put(commandData);

            ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_select", player).open();
        });
    }

    @Override
    public int getLastPage() {
        if(ArrowCore.CONFIG.isDebug)
            ArrowCommands.LOGGER.error("Items Size is: {}", items.size());
        return Math.floorDiv(items.size(), 15);
    }
}
