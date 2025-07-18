package dev.elrol.arrow.commands.menus.createshop.pokemon;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.commands.data.PokemonShopSaleData;
import dev.elrol.arrow.commands.data.ShopData;
import dev.elrol.arrow.commands.data.TempShopData;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.libs.CobblemonUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class PokemonShopSetupMenu extends _CommandMenuBase {

    public PokemonShopSetupMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();


        PlayerPartyStore party = CobblemonUtils.getParty(player);

        for(int i = 0 ; i < party.size() ; i++) {
            Pokemon slot = party.get(i);

            if (slot != null) {

                GuiElementBuilder element = new GuiElementBuilder(PokemonItem.from(slot))
                        .setName(slot.getNickname() == null ? slot.getDisplayName() : slot.getNickname())
                        .setCallback(() -> {
                            click();
                            ShopData shopData = new ShopData(player.getUuid());
                            shopData.saleData = new PokemonShopSaleData();
                            commandData.playerShopData.tempShop = new TempShopData(shopData);
                            data.put(commandData);
                        });
                menu.setSlot(19 + ((i > 2) ? 1 : 0) + i, CobblemonUtils.addPokeStatElement(element, slot));
            }
        }
    }

    @Override
    public int getMenuID() {
        return 113;
    }

    @Override
    public char getMenuUnicode() {
        return '⎤';
    }

    @Override
    public String getMenuName() {
        return "pokemon_shop_setup";
    }
}
