package dev.elrol.arrow.commands.menus;

import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.libs.DaycareUtils;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.*;
import dev.elrol.arrow.registries.CoreMenuItems;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.util.Tristate;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class DaycareMenu extends _CommandMenuBase {

    public static final Map<UUID, DaycareMenu> daycareMenus = new HashMap<>();

    private GuiElementBuilder hatcheryElement;

    public DaycareMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    public void open() {
        super.open();
        if(commandData.daycareData.isBreeding()) {
            if(ArrowCore.CONFIG.isDebug) ArrowCommands.LOGGER.warn("Daycare Time Left: {}", commandData.daycareData.getTime());
            daycareMenus.put(player.getUuid(), this);
        }
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        int slot1 = commandData.daycareData.slot1;
        int slot2 = commandData.daycareData.slot2;

        if(menu.getAutoUpdate()) {
            if(ArrowCore.CONFIG.isDebug) ArrowCommands.LOGGER.warn("Auto Update Enabled");
        } else {
            if(ArrowCore.CONFIG.isDebug) ArrowCommands.LOGGER.warn("Auto Update Disabled");
        }

        GuiElementBuilder slot1Element = MenuUtils.item(CommandsMenuItems.SLOT_ONE_BUTTON, 1, "spot_one");
        GuiElementBuilder slot2Element = MenuUtils.item(CommandsMenuItems.SLOT_TWO_BUTTON, 1, "spot_two");
        hatcheryElement = MenuUtils.item(CommandsMenuItems.NEST_BUTTON, 1, "hatchery");

        slot1Element.setCallback(() -> {
            click();
            navigateToMenu("pokeselect_1");
        });
        slot2Element.setCallback(() -> {
            click();
            navigateToMenu("pokeselect_2");
        });

        if(slot1 > -1) {
            if(ArrowCore.CONFIG.isDebug) ArrowCore.LOGGER.warn("Slot1 was greater than 0");
            Pokemon pokemon1 = CobblemonUtils.getSlot(player, slot1);
            if(pokemon1 != null) {
                slot1Element.setItem(Items.SNOWBALL);
                slot1Element.setName(pokemon1.getNickname() == null ? pokemon1.getDisplayName() : pokemon1.getNickname());
                CobblemonUtils.addPokeStatElement(slot1Element, pokemon1);
                slot1Element.setCustomModelData(Constants.getPokeballID(pokemon1.getCaughtBall()));
                setSlot(21, slot1Element.build());
            } else {
                setSlot(21, slot1Element);
            }
        } else {
            setSlot(21, slot1Element);
        }

        if(slot2 > -1) {
            if(ArrowCore.CONFIG.isDebug) ArrowCore.LOGGER.warn("Slot2 was greater than 0");
            Pokemon pokemon2 = CobblemonUtils.getSlot(player, slot2);
            if(pokemon2 != null) {
                slot2Element.setItem(Items.SNOWBALL);
                slot2Element.setName(pokemon2.getNickname() == null ? pokemon2.getDisplayName() : pokemon2.getNickname());
                CobblemonUtils.addPokeStatElement(slot2Element,pokemon2);
                slot2Element.setCustomModelData(Constants.getPokeballID(pokemon2.getCaughtBall()));
                setSlot(23, slot2Element.build());
            } else {
                setSlot(23, slot2Element);
            }
        } else {
            setSlot(23, slot2Element);
        }

        setHatchery();

        setSlot(25, MenuUtils.item(commandData.daycareData.isBreeding() ? CommandsMenuItems.REMOVE_BUTTON : CommandsMenuItems.BREED_BUTTON, 1, "breed").setCallback(()->{
            Pokemon poke1 = CobblemonUtils.getSlot(player, commandData.daycareData.slot1);
            Pokemon poke2 = CobblemonUtils.getSlot(player, commandData.daycareData.slot2);

            if(poke1 != null && poke2 != null && DaycareUtils.canPokemonBreed(poke1, poke2) && !commandData.daycareData.isBreeding()) {
                click();
                daycareMenus.put(player.getUuid(), this);
                commandData.daycareData.setTime((int)(ArrowCommands.CONFIG.daycareSettings.minutesToHatchEgg * 60.0f));
                commandData.daycareData.setEgg(DaycareUtils.breed(player, poke1, poke2), player);
                setHatchery();
            }
        }));
    }

    public TriState tickEgg() {
        TriState state = commandData.daycareData.tickTime();
        data.put(commandData);
        setHatchery();
        return state;
    }

    private void setHatchery() {
        boolean isBreeding = commandData.daycareData.isBreeding();
        boolean isReady = commandData.daycareData.isReadyToHatch();

        if(isReady) {
            hatcheryElement.setItem(Items.SNOWBALL);
            Pokemon egg = commandData.daycareData.getEgg();
            CobblemonUtils.addPokeStatElement(hatcheryElement, egg);
            assert egg != null;
            int modelData = Constants.getPokeballID(egg.getCaughtBall());
            hatcheryElement.setCallback(() -> {
                click();
                commandData.daycareData.hatchEgg(player);
                data.put(commandData);
                setHatchery();
            });

            if (ArrowCore.CONFIG.isDebug) {
                hatcheryElement.addLoreLine(Text.literal("" + modelData));
            }

            menu.setSlot(19, hatcheryElement.setCustomModelData(modelData));
        } else {
            if(ArrowCore.CONFIG.isDebug)
                CobblemonUtils.addPokeStatElement(hatcheryElement, commandData.daycareData.getEgg());
            if(commandData.daycareData.getTime() >= 0) {
                hatcheryElement.setItem(CommandsMenuItems.EGG_BUTTON);
                hatcheryElement.setCallback(() -> {});
                hatcheryElement.setLore(getLore());
            } else {
                hatcheryElement.setItem(CommandsMenuItems.NEST_BUTTON);
                hatcheryElement.setLore(new ArrayList<>());
            }
            setSlot(19, hatcheryElement);
        }

    }

    @Override
    public int getMenuID() {
        return 2;
    }

    @Override
    public char getMenuUnicode() {
        return '≂';
    }

    @Override
    public String getMenuName() {
        return "daycare";
    }

    public List<Text> getLore() {
        List<Text> lore = new ArrayList<>();

        lore.add(ModTranslations.translate("arrow.menu.item.daycare.egg_not_ready").formatted(Formatting.RED));
        lore.add(ModTranslations.translate("arrow.menu.item.daycare.time_left", ModUtils.formatSeconds(commandData.daycareData.getTime())).formatted(Formatting.DARK_GREEN));
        return lore;
    }
}
