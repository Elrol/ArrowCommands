package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.CommandConfig;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class ShopMenu extends _CommandPageMenuBase {

    Map<String, CommandConfig.ShopItems> map = new HashMap<>();

    public ShopMenu(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        for(Map.Entry<String, CommandConfig.ShopItems> e : ArrowCommands.CONFIG.shopSettings.shops.entrySet()) {
            if(PermUtils.hasPerm(player, "arrow.shop", (e.getKey().toLowerCase())).asBoolean()) {
                map.put(e.getKey(), e.getValue());
            }
        }

        drawItems(map);

        setSlot(0, MenuUtils.item(CommandsMenuItems.CART_BUTTON, 1, ModTranslations.translate("arrow.menu.shop.cart").formatted(Formatting.BOLD)).setCallback(() -> {
            click();
            navigateToMenu("shopping_cart");
        }));
    }

    @Override
    public int getMenuID() {
        return 8;
    }

    @Override
    public char getMenuUnicode() {
        return '≈';
    }

    @Override
    public String getMenuName() {
        return "shop";
    }

    @Override
    protected <T> GuiElementBuilder createElement(String key, Map<String, T> map) {
        CommandConfig.ShopItems shopItems = (CommandConfig.ShopItems) map.get(key);
        GuiElementBuilder in = MenuUtils.itemStack(shopItems.shopIcon, key);
        int modelData = (shopItems.shopID * 1000000) + (getMenuID() * 1000);
        in.setCustomModelData(modelData);
        in.setName(shopItems.name);

        if(ArrowCore.CONFIG.isDebug)
            in.addLoreLine(ModTranslations.translate("arrow.menu.item.model_data", modelData).formatted(Formatting.RED));
        return in.setCallback(() -> {
            click();
            commandData.shoppingData.currentCart = new ListingData();
            commandData.shoppingData.shop = key;
            commandData.shoppingData.page = 0;
            data.put(commandData);

            ArrowCommands.LOGGER.error("Is Codec null: {}", commandData.getCodec() == null);

            ArrowCore.INSTANCE.getPlayerDataRegistry().save(player.getUuid(), data);
            navigateToMenu("item_shop");
        });
    }

    @Override
    public int getLastPage() {
        return Math.floorDiv(map.size(),15);
    }
}
