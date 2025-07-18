package dev.elrol.arrow.commands.menus;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.registries.CoreMenuItems;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PokeSelectMenu extends _CommandMenuBase {

    private PokemonSelected selectedFunction;
    private Runnable canceledFunction;

    public PokeSelectMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
        menu.setCanClose(false);
    }

    @Override
    protected void drawMenu() {
        PlayerPartyStore party = CobblemonUtils.getParty(player);

        for(int i = 0 ; i < party.size() ; i++) {
            Pokemon slot = party.get(i);

            if(slot != null) {
                int slotIndex = i;

                GuiElementBuilder element = new GuiElementBuilder(PokemonItem.from(slot))
                        .setName(slot.getNickname() == null ? slot.getDisplayName() : slot.getNickname())
                        .setCallback(() -> {
                            click();
                            if(selectedFunction != null) selectedFunction.selected(slot, slotIndex);
                        });
                menu.setSlot(19 + i, CobblemonUtils.addPokeStatElement(element, slot));

                menu.setSlot(25, MenuUtils.item(CoreMenuItems.BACK_BUTTON, 1, ModTranslations.err("confirm_cancel")).setCallback(() -> {
                    click();
                    if(canceledFunction != null) canceledFunction.run();
                }));
            }
        }
    }

    @Override
    public int getMenuID() {
        return 0;
    }

    @Override
    public @NotNull String getMenuName() {
        return "poke_select";
    }

    @Override
    public char getMenuUnicode() {
        return '≅';
    }

    public void setSelectFunction(PokemonSelected function) {
        this.selectedFunction = function;
    }

    public void setCanceledFunction(Runnable runnable) {
        this.canceledFunction = runnable;
    }

    @FunctionalInterface
    public interface PokemonSelected {
        void selected(Pokemon pokemon, int slot);
    }
}
