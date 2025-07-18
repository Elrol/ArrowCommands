package dev.elrol.arrow.commands.menus;

import com.cobblemon.mod.common.CobblemonItems;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.*;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.menus._MenuBase;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ShopSetupMenu extends _CommandMenuBase {

    public ShopSetupMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        setSlot(20, MenuUtils.item(Items.APPLE, 1, Text.literal("Item Shop")).setCallback(() -> {
            newShop("item");
        }));

        setSlot(24, MenuUtils.item(CobblemonItems.POKE_BALL, 1, Text.literal("Pokemon Shop")).setCallback(() -> {
            newShop("pokemon");
        }));

    }

    private void newShop(String type) {
        click();

        ShopData shopData = new ShopData(player.getUuid());

        ShopSaleData saleData;

        switch (type.toLowerCase()) {
            case "item": {
                saleData = new ItemShopSaleData();
                break;
            }
            case "pokemon": {
                saleData = new PokemonShopSaleData();
                break;
            }
            default: {
                ArrowCommands.LOGGER.error("Invalid Shop Type: {}", type);
                return;
            }
        }

        shopData.saleData = saleData;
        commandData.playerShopData.tempShop = new TempShopData(shopData);
        data.put(commandData, true);

        if(data.get(new PlayerDataCommands()).playerShopData.tempShop != null) {
            ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", player).open(true);
        }

    }

    @Override
    public int getMenuID() {
        return 110;
    }

    @Override
    public char getMenuUnicode() {
        return '║';
    }

    @Override
    public String getMenuName() {
        return "shop_setup";
    }
}
