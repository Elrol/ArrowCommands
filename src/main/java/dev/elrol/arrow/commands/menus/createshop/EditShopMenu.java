package dev.elrol.arrow.commands.menus.createshop;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.*;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.libs.BlockUtils;
import dev.elrol.arrow.commands.menus.PokeSelectMenu;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.commands.libs.PlayerShopUtils;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EditShopMenu extends _CommandMenuBase {

    ShopSaleData.ShopSaleDataType<?> type;

    public EditShopMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X6);
        menu.setCanClose(false);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        TempShopData shopData = commandData.playerShopData.tempShop;
        ShopData shop = shopData.shop;

        TriState isSelling = shop.getIsSelling();

        ItemStack displayItem = shop.saleData.getDisplayItem();
        type = shop.saleData.getType();

        List<BlockPos> stockList = new ArrayList<>();
        Pokemon salePokemon = null;
        if(shop.saleData instanceof ItemShopSaleData itemShopSaleData) {
            stockList = new ArrayList<>(itemShopSaleData.getStock());
        } else if(shop.saleData instanceof PokemonShopSaleData pokemonShopSaleData) {
            salePokemon = pokemonShopSaleData.getPokemon();
        }

        boolean hasPrice = shop.getPrice() > 0;
        boolean hasDisplayCase = shop.getDisplayCase() != null;
        boolean hasShopItem = shopData.hasDisplayItem();
        boolean hasSellingSet = !isSelling.equals(TriState.DEFAULT);
        boolean hasStock = !stockList.isEmpty();
        boolean hasShopPokemon = salePokemon != null;

        boolean isItemShop = type.equals(ShopSaleDataTypes.ITEM_SHOP);
        boolean isPokeShop = type.equals(ShopSaleDataTypes.POKEMON_SHOP);

        // Sell Button
        setSlot(20, MenuUtils.item(isSelling.equals(TriState.TRUE) ? CommandsMenuItems.SELL_BUTTON_SELECTED : CommandsMenuItems.SELL_BUTTON, 1, Text.literal("Sell")).setCallback(() -> {
            //ToDo change this to allow player to choose to buy pokemon
            if(isPokeShop) return;
            click();
            if(!isSelling.equals(TriState.TRUE)) {
                commandData.playerShopData.tempShop.shop.setIsSelling(TriState.TRUE);
                data.put(commandData);
                drawMenu();
            }
        }));

        // Display Case Button
        List<Text> displayCaseLore = new ArrayList<>();
        displayCaseLore.add(ModTranslations.literal(hasDisplayCase ? "Display Case is at: " + shop.getDisplayCase().toShortString() : "Display Case hasn't been set.").formatted(hasDisplayCase ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        setSlot(22, MenuUtils.itemWithLore(CobblemonItems.DISPLAY_CASE, 1, CobblemonItems.DISPLAY_CASE.getName(), displayCaseLore).setCallback(() -> {
            if(hasDisplayCase) return;
            click();
            player.sendMessage(ModTranslations.msg("place_display_case"));
            commandData.playerShopData.tempShop.setStage(TempShopData.ShopStage.displayCase);
            data.put(commandData);
            close();
        }));

        // Stock Button
        if(isItemShop) {
            List<Text> stockLore = new ArrayList<>();
            if(hasStock) stockList.forEach(stock -> stockLore.add(ModTranslations.literal(stock.toShortString()).formatted(Formatting.DARK_GREEN)));
            else stockLore.add(ModTranslations.err("missing_stock"));

            setSlot(24, MenuUtils.itemWithLore(Items.CHEST, 1, "stock", stockLore).setCallback(() -> {
                click();
                player.sendMessage(ModTranslations.msg("interact_with_storage"));
                commandData.playerShopData.tempShop.setStage(TempShopData.ShopStage.stock);
                data.put(commandData);
                close();
            }));
        }

        // Buying Button
        // TODO enable buying of pokemon with certain requirements
        setSlot(29, MenuUtils.item(isPokeShop ? CommandsMenuItems.GRAY_BUTTON : isSelling.equals(TriState.FALSE) ? CommandsMenuItems.BUY_BUTTON_SELECTED : CommandsMenuItems.BUY_BUTTON, 1, Text.literal("Buy")).setCallback(() -> {
            if(isPokeShop) return;
            click();
            if(!isSelling.equals(TriState.FALSE)) {
                commandData.playerShopData.tempShop.shop.setIsSelling(TriState.FALSE);
                data.put(commandData);
                drawMenu();
            }
        }));

        //Shop Item Button
        GuiElementBuilder saleItem = MenuUtils.itemStack(hasShopItem ? displayItem : new ItemStack(Items.BARRIER, 1), hasShopItem ? displayItem.getName() : ModTranslations.info("for_sale").formatted(Formatting.RED)).setCallback(() -> {
            if(hasDisplayCase && isItemShop) {
                click();
                player.sendMessage(ModTranslations.msg("use_item_on_case"));
                commandData.playerShopData.tempShop.setStage(TempShopData.ShopStage.saleData);
                data.put(commandData);
                close();
            } else if(isPokeShop) {
                click();
                PokeSelectMenu selectMenu = (PokeSelectMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("poke_select", player);
                selectMenu.setSelectFunction((pokemon, slot) -> {
                    PokemonShopSaleData pokemonSaleData = (PokemonShopSaleData) commandData.playerShopData.tempShop.shop.saleData;
                    pokemonSaleData.setPokemon(pokemon);
                    commandData.playerShopData.tempShop.shop.saleData = pokemonSaleData;
                    data.put(commandData);
                    openEditMenu();
                });
                selectMenu.setCanceledFunction(this::openEditMenu);
                selectMenu.open();
            }
        });

        if(!hasShopItem) {
            List<Text> itemLore = new ArrayList<>();
            itemLore.add(ModTranslations.info("no_sale_item").formatted(Formatting.DARK_RED));
            saleItem.setLore(itemLore);
        }

        setSlot(31, saleItem);

        // Price Button
        List<Text> priceLore = new ArrayList<>();
        priceLore.add(ModTranslations.literal(hasPrice ? "Price is " + shop.getFormatedPrice() : "Price is unset").formatted(hasPrice ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        setSlot(33, MenuUtils.itemWithLore(hasPrice ? CommandsMenuItems.LIME_BUTTON : CommandsMenuItems.RED_BUTTON, 1, Text.literal("Set Price").formatted(hasPrice ? Formatting.GREEN : Formatting.RED), priceLore).setCallback(() -> {
            click();
            if(commandData.playerShopData.tempShop != null) {
                commandData.playerShopData.tempShop.setStage(TempShopData.ShopStage.price);
                data.put(commandData);
                player.sendMessage(ModTranslations.msg("type_price"));
                close();
            }
        }));

        // Cancel Button
        setSlot(37, cancel(CommandsMenuItems.RED_BUTTON_LEFT));
        setSlot(38, cancel(CommandsMenuItems.RED_BUTTON_MIDDLE));
        setSlot(39, cancel(CommandsMenuItems.RED_BUTTON_RIGHT));

        // Confirm Button
        setSlot(41, save(CommandsMenuItems.LIME_BUTTON_LEFT,   CommandsMenuItems.RED_BUTTON_LEFT,   hasPrice, hasSellingSet, hasDisplayCase, hasShopItem, hasStock, hasShopPokemon));
        setSlot(42, save(CommandsMenuItems.LIME_BUTTON_MIDDLE, CommandsMenuItems.RED_BUTTON_MIDDLE, hasPrice, hasSellingSet, hasDisplayCase, hasShopItem, hasStock, hasShopPokemon));
        setSlot(43, save(CommandsMenuItems.LIME_BUTTON_RIGHT,  CommandsMenuItems.RED_BUTTON_RIGHT,  hasPrice, hasSellingSet, hasDisplayCase, hasShopItem, hasStock, hasShopPokemon));

    }

    private void openEditMenu() {
        ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", player).open(true);
    }

    private GuiElementBuilder save(Item enabled, Item disabled, boolean hasPrice, boolean hasShopMode, boolean hasDisplayCase, boolean hasShopItem, boolean hasStock, boolean hasPokemon) {
        boolean canSave = hasPrice && hasShopMode && hasDisplayCase && hasShopItem;

        List<Text> lore = new ArrayList<>();
        lore.add(ModTranslations.info("shop_mode").formatted(hasShopMode ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        lore.add(ModTranslations.info("shop_display_case").formatted(hasDisplayCase ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        if(type.equals(ShopSaleDataTypes.ITEM_SHOP)) {
            lore.add(ModTranslations.info("shop_stock").formatted(hasStock ? Formatting.DARK_GREEN : Formatting.DARK_RED));
            lore.add(ModTranslations.info("shop_item").formatted(hasShopItem ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        } else if(type.equals(ShopSaleDataTypes.POKEMON_SHOP)) {
            lore.add(ModTranslations.info("shop_pokemon").formatted(hasPokemon ? Formatting.DARK_GREEN : Formatting.DARK_RED));
        }
        lore.add(ModTranslations.info("shop_price").formatted(hasPrice ? Formatting.DARK_GREEN : Formatting.DARK_RED));

        GuiElementBuilder element = MenuUtils.item(canSave ? enabled : disabled, 1, Text.literal("Confirm").formatted(canSave ? Formatting.GREEN : Formatting.RED)).setCallback(() -> {
            if(canSave) {
                click();
                BlockPos pos = commandData.playerShopData.tempShop.shop.getDisplayCase();
                ItemStack stack = commandData.playerShopData.tempShop.shop.saleData.getDisplayItem();
                if(PlayerShopUtils.createShop(player)) {
                    BlockEntity entity = player.getServerWorld().getBlockEntity(pos);
                    if(entity instanceof DisplayCaseBlockEntity caseEntity) {
                        IDisplayShop displayShop = BlockUtils.getDisplayShop(caseEntity);

                        if(displayShop != null) {
                            displayShop.arrowcommands$setOwner(player.getUuid());
                            displayShop.arrowcommands$lock();
                            ArrowCommands.debug("Is locked: " + displayShop.arrowcommands$locked());

                            caseEntity.setStack(0, stack);
                            player.sendMessage(ModTranslations.msg("shop_created"));
                        }
                    } else {
                        player.sendMessage(ModTranslations.err("display_case_missing"));
                    }
                } else {
                    player.sendMessage(ModTranslations.err("shop_exists"));
                }
                close();
            }
        });
        element.setLore(lore);
        return element;
    }

    private GuiElementBuilder cancel(Item item) {
        return MenuUtils.item(item, 1, Text.literal("Cancel").formatted(Formatting.RED)).setCallback(()->{
            click();
            returnToMenu();
        });
    }

    @Override
    public void returnToMenu() {
        commandData.playerShopData.tempShop.setStage(TempShopData.ShopStage.cancel);
        data.put(commandData);
        close();
    }

    @Override
    public int getMenuID() {
        return 111;
    }

    @Override
    public char getMenuUnicode() {
        return '⎥';
    }

    @Override
    public @NotNull String getMenuName() {
        return "edit_shop_menu";
    }
}
