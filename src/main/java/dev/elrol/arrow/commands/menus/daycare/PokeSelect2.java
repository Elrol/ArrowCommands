package dev.elrol.arrow.commands.menus.daycare;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.libs.DaycareUtils;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.Constants;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.menus._MenuBase;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PokeSelect2 extends _CommandMenuBase {

    public PokeSelect2(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        PlayerPartyStore party = CobblemonUtils.getParty(player);

        for(int i = 0 ; i < party.size() ; i++) {
            Pokemon slot = party.get(i);
            int slot1Index = commandData.daycareData.slot1;
            int slot2Index = commandData.daycareData.slot2;

            if(slot != null) {
                int slotIndex = i;

                if(slot2Index > -1 && slot2Index == slotIndex) {
                    int slotOffset = ((i > 2) ? 1 : 0) + i;
                    setSlot(19 + slotOffset, MenuUtils.item(CommandsMenuItems.REMOVE_BUTTON, 1, Text.literal("Remove").formatted(Formatting.RED))
                            .setCallback(() -> {
                                click();
                                commandData.daycareData.slot2 = -1;
                                data.put(commandData);
                                returnToMenu();
                            })
                    );
                    return;
                }

                if(slot1Index > -1 && slot1Index == slotIndex) {
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.warn("Pokemon already in slot one");
                    continue;
                }

                if(slot1Index > -1) {
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.warn("Breeding Selection Being Filtered");
                    Pokemon slot1 = CobblemonUtils.getSlot(player, slot1Index);
                    assert slot1 != null;
                    if(!DaycareUtils.canPokemonBreed(slot1, slot)) continue;
                }

                GuiElementBuilder element = new GuiElementBuilder(Items.SNOWBALL, 1)
                        .setName(slot.getNickname() == null ? slot.getDisplayName() : slot.getNickname())
                        .setCustomModelData(Constants.getPokeballID(slot.getCaughtBall()))
                        .setCallback(() -> {
                            click();
                            commandData.daycareData.slot2 = slotIndex;
                            data.put(commandData);
                            returnToMenu();
                        });

                menu.setSlot(19 + ((i > 2) ? 1 : 0) + i, CobblemonUtils.addPokeStatElement(element, slot));
            }
        }
    }

    @Override
    public int getMenuID() {
        return 6;
    }

    @Override
    public char getMenuUnicode() {
        return '≆';
    }

    @Override
    public String getMenuName() {
        return "pokeselect_2";
    }
}
