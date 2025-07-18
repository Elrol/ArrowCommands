package dev.elrol.arrow.commands.menus.daycare;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.libs.DaycareUtils;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.Constants;
import dev.elrol.arrow.libs.MenuUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class PokeSelect1 extends _CommandMenuBase {

    public PokeSelect1(ServerPlayerEntity player) {
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

                if(slot1Index > -1 && slot1Index == slotIndex) {
                    int slotOffset = ((i > 2) ? 1 : 0) + i;
                    setSlot(19 + slotOffset, MenuUtils.item(CommandsMenuItems.REMOVE_BUTTON, 1, Text.literal("Remove").formatted(Formatting.RED))
                            .setCallback(() -> {
                                click();
                                commandData.daycareData.slot1 = -1;
                                data.put(commandData);
                                returnToMenu();
                            })
                    );
                    return;
                }

                if(slot2Index > -1 && slot2Index == slotIndex) {
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.warn("Pokemon already in slot two");
                    continue;
                }

                if(slot2Index > -1) {
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.warn("Breeding Selection Being Filtered");
                    Pokemon slot2 = CobblemonUtils.getSlot(player, slot2Index);
                    assert slot2 != null;
                    if(!DaycareUtils.canPokemonBreed(slot, slot2)) continue;
                }

                GuiElementBuilder element = new GuiElementBuilder(PokemonItem.from(slot))
                        .setName(slot.getNickname() == null ? slot.getDisplayName() : slot.getNickname())
                        .setCallback(() -> {
                            click();
                            commandData.daycareData.slot1 = slotIndex;
                            data.put(commandData);
                            returnToMenu();
                        });
                menu.setSlot(19 + ((i > 2) ? 1 : 0) + i, CobblemonUtils.addPokeStatElement(element, slot));
            }
        }
    }

    @Override
    public int getMenuID() {
        return 5;
    }

    @Override
    public char getMenuUnicode() {
        return '≅';
    }

    @Override
    public @NotNull String getMenuName() {
        return "pokeselect_1";
    }
}
