package dev.elrol.arrow.commands.menus.shops;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.data.ServerShopData;
import dev.elrol.arrow.commands.data.ServerShopItem;
import dev.elrol.arrow.commands.menus._CommandPageMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.commands.registries.ServerShopRegistry;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import dev.elrol.arrow.menus._MenuBase;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemShopMenu extends _CommandPageMenuBase {
    String shopName;
    ServerShopData shopItems;
    Map<String, ServerShopItem> items = new HashMap<>();

    public ItemShopMenu(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    protected void drawMenu() {
        // Sets the name of the shop
        shopName = commandData.shoppingData.shop;

        if(shopName.isEmpty()) {
            ArrowCommands.LOGGER.error("Shop Name was missing");
            return;
        }

        // Gets the items the shop sells
        shopItems = ServerShopRegistry.get(shopName);

        ArrowCommands.debug("Shop Name: " + shopName);
        if(shopItems != null) {
            if(shopItems.itemShop.isEmpty()) {
                ArrowCommands.LOGGER.error("Item Shop {} has no items to sell", shopName);
            }

            // Check if the player has the permission needed to see each listing
            for(int i = 0; i < shopItems.itemShop.size(); i++) {
                ServerShopItem item = shopItems.itemShop.get(i);
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
    public @NotNull String getMenuName() {
        return "item_shop";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> GuiElementBuilder createElement(String key, Map<String, T> map) {
        ServerShopItem shopItem = ((Map<String, ServerShopItem>) map).get(key);
        ItemStack item = shopItem.item;
        GuiElementBuilder in = MenuUtils.itemStack(item.copyWithCount(1), item.getName());

        in.addLoreLine(ModTranslations.literal(ArrowCore.INSTANCE.getEconomyRegistry().formatAmount(shopItem.cost)).formatted(Formatting.GREEN));
        return in.setCallback(() -> {
            click();

            // Create a new cart using the selected item
            commandData.shoppingData.currentCart = new ListingData(item, shopItem.cost, 0);
            data.put(commandData);

            // Create a new ItemSelect Menu to allow the player to shop
            ItemSelectMenu selectMenu = (ItemSelectMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_select", player);
            selectMenu.setConfirmFunction((listing) -> {
                click();

                // Confirms the current cart and adds it to the shopping cart
                PlayerData data1 = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                PlayerDataCommands commandData1 = data1.get(new PlayerDataCommands());
                commandData1.shoppingData.currentCart = listing;
                commandData1.shoppingData.confirmCurrentCart();

                // Open the Item Shop menu again
                _MenuBase menu = ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_shop", player);
                if(menu == null) {
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.error("Item Shop Menu was null when being created");
                    return;
                }
                data1.put(commandData1);
                menu.open();
            });

            selectMenu.setCancelFunction(() -> {
                click();

                // Clears the current cart and returns to the Item Shop menu
                PlayerData data1 = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                PlayerDataCommands commandData1 = data1.get(new PlayerDataCommands());
                commandData1.shoppingData.cancelCurrentCart();
                data1.put(commandData1);
                Objects.requireNonNull(ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_shop", player)).open();
            });

            selectMenu.open(commandData.shoppingData.currentCart, false);
        });
    }

    @Override
    public int getLastPage() {
        if(ArrowCore.CONFIG.isDebug)
            ArrowCommands.LOGGER.error("Items Size is: {}", items.size());
        return Math.floorDiv(items.size(), 15);
    }
}
