package dev.elrol.arrow.commands.menus.shops;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ListingData;
import dev.elrol.arrow.commands.data.ShoppingData;
import dev.elrol.arrow.commands.menus._CommandMenuBase;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.data.ArrowPlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemSelectMenu extends _CommandMenuBase {

    ShoppingData shoppingData;
    GuiElementBuilder shopItemElement;
    ListingData listingData;

    ItemSelectFunction confirmFunction;
    Runnable cancelFunction;
    boolean checkCanAfford = false;
    UUID shopOwner = null;

    ArrowPlayerData ownerPlayerData;
    PlayerDataCore ownerCoreData;
    Text ownerName = Text.empty();

    public ItemSelectMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
        shoppingData = commandData.shoppingData;
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        if(!listingData.isEmpty()) {
            int current = listingData.getUnits();
            int max = listingData.getMaxUnits() == -1 ? Math.floorDiv((36  * listingData.getItem().getMaxCount()), listingData.getItem().getCount()) : listingData.getMaxUnits();

            shopItemElement = MenuUtils.itemStack(
                    listingData.getItem(),
                    listingData.getItem().getName());
            shopItemElement.addLoreLine(ModTranslations.translate("arrow.menu.shop.select.amount").formatted( Formatting.GREEN).append(ModTranslations.literal (" " + current).formatted(Formatting.GRAY)));
            shopItemElement.addLoreLine(ModTranslations.translate("arrow.menu.shop.select.cost").formatted(Formatting.GREEN).append(ModTranslations.literal(" " + listingData.getPriceString()).formatted(Formatting.GRAY)));

            if(current - 64 >= listingData.getMinUnits()) setSlot(18, changeAmount(CommandsMenuItems.RED_BUTTON_4, -64)); else menu.clearSlot(18);
            if(current - 16 >= listingData.getMinUnits()) setSlot(19, changeAmount(CommandsMenuItems.RED_BUTTON_3, -16)); else menu.clearSlot(19);
            if(current - 4 >= listingData.getMinUnits())  setSlot(20, changeAmount(CommandsMenuItems.RED_BUTTON_2, -4)); else menu.clearSlot(20);
            if(current - 1 >= listingData.getMinUnits())  setSlot(21, changeAmount(CommandsMenuItems.RED_BUTTON_1, -1)); else menu.clearSlot(21);

            setSlot(22, shopItemElement);

            if(current + 1 <= max)  setSlot(23, changeAmount(CommandsMenuItems.LIME_BUTTON_1, 1)); else menu.clearSlot(23);
            if(current + 4 <= max)  setSlot(24, changeAmount(CommandsMenuItems.LIME_BUTTON_2, 4)); else menu.clearSlot(24);
            if(current + 16 <= max) setSlot(25, changeAmount(CommandsMenuItems.LIME_BUTTON_3, 16)); else menu.clearSlot(25);
            if(current + 64 <= max) setSlot(26, changeAmount(CommandsMenuItems.LIME_BUTTON_4, 64)); else menu.clearSlot(26);

            setSlot(29, cancel(CommandsMenuItems.RED_BUTTON_LEFT));
            setSlot(30, cancel(CommandsMenuItems.RED_BUTTON_RIGHT));

            setSlot(32, confirm(CommandsMenuItems.LIME_BUTTON_LEFT, CommandsMenuItems.GRAY_BUTTON_LEFT));
            setSlot(33, confirm(CommandsMenuItems.LIME_BUTTON_RIGHT, CommandsMenuItems.GRAY_BUTTON_RIGHT));
        }
    }

    @Override
    public void open() {
        ArrowCommands.LOGGER.error("Item Select Menu was attempted to be opened without having a listing set.");
    }

    public void open(ListingData listingData, boolean clearHistory, boolean checkCanAfford, ItemSelectFunction selectFunction, Runnable cancelFunction) {
        this.listingData = listingData;
        this.confirmFunction = selectFunction;
        this.cancelFunction = cancelFunction;
        this.checkCanAfford = checkCanAfford;
        super.open(clearHistory);
    }

    public GuiElementBuilder changeAmount(Item item, int amount) {
        boolean isPositive = amount > 0;
        int units = listingData.getUnits();
        Formatting format = (isPositive ? Formatting.GREEN : Formatting.RED);
        int targetAmount = Math.max(units + amount, listingData.getMinUnits());
        IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        GuiElementBuilder element = MenuUtils.item(item, 1, ModTranslations.literal((isPositive ? "+" : "") + amount).formatted(format, Formatting.BOLD)).setCallback(()->{
            listingData.changeUnits(amount);
            click();
            drawMenu();
        });

        element.addLoreLine(ModTranslations.literal(units + " ─> " + targetAmount).formatted(format));
        element.addLoreLine(ModTranslations.literal(listingData.getPriceString() + " -> " + econRegistry.formatAmount(listingData.getPricePerUnit() * targetAmount)).formatted(format));

        return element;
    }

    public GuiElementBuilder cancel(Item item) {
        return MenuUtils.item(item, 1, ModTranslations.translate("arrow.menu.shop.select.cancel").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback(() -> {
                    if(cancelFunction != null) {
                        click();
                        cancelFunction.run();
                    }
                });
    }

    public GuiElementBuilder confirm(Item item, Item disabled) {
        int units = commandData.shoppingData.currentCart.getUnits();
        int count = listingData.getItem().getCount();
        IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        boolean canAfford = !checkCanAfford || (econRegistry.canAfford((listingData.isSelling() ? player.getUuid() : shopOwner), BigDecimal.valueOf(listingData.getTotalPrice())));
        boolean flag = units > 0 && canAfford;

        GuiElementBuilder element = MenuUtils.item((flag ? item : disabled), 1, ModTranslations.translate(checkCanAfford ? "arrow.menu.shop.cart.confirm" : "arrow.menu.shop.select.add").formatted(Formatting.BOLD, (flag ? Formatting.GREEN : Formatting.DARK_GRAY)))
                .setCallback(() -> {
                    if(!flag) return;
                    if(confirmFunction != null) {
                        click();
                        confirmFunction.select(listingData);
                    }
                });


        double total = listingData.getTotalPrice();
        BigDecimal balance = econRegistry.getBal(player.getUuid());
        String balString = "  " + econRegistry.formatAmount(balance);
        String totalString = (listingData.isSelling() ? "- " : "+ ") + econRegistry.formatAmount(total);

        List<Text> lore = new ArrayList<>();

        if(checkCanAfford) {
            BigDecimal totalPrice = BigDecimal.valueOf(total);
            Formatting format = flag ? Formatting.DARK_GREEN : Formatting.RED;

            lore.add(ModTranslations.literal(balString).formatted(format));
            lore.add(ModTranslations.literal(totalString).formatted(format));
            lore.add(ModTranslations.literal("─".repeat(Math.max(balString.length(), totalString.length()))).formatted(Formatting.GRAY));
            lore.add(ModTranslations.literal((canAfford ? "  " : "- ") + (canAfford || listingData.isSelling() ? econRegistry.formatAmount(listingData.isSelling() ? balance.subtract(totalPrice) : balance.add(totalPrice)) : ownerName.getString() + " can't afford this")).formatted(format));
        } else {
            lore.add(ModTranslations.translate("arrow.menu.shop.select.amount").formatted(Formatting.GREEN)
                    .append(ModTranslations.literal(" " + units).formatted(Formatting.GRAY))
                    .append(count > 1 ? ModTranslations.literal(" (x" + count + " = " + (count * units) + ")").formatted(Formatting.DARK_GRAY) : Text.empty()));
            lore.add(ModTranslations.translate("arrow.menu.shop.select.cost").formatted(Formatting.GREEN)
                    .append(ModTranslations.literal(" " + listingData.getPriceString()).formatted(Formatting.GRAY)));

        }

        return element.setLore(lore);
    }

    public void setShopOwner(@NotNull UUID shopOwner) {
        this.shopOwner = shopOwner;
        ownerPlayerData = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(shopOwner);
        ownerCoreData = ownerPlayerData.get(new PlayerDataCore());
        ownerName = ownerCoreData.username;
    }

    @Override
    public int getMenuID() {
        return 10;
    }

    @Override
    public char getMenuUnicode() {
        return '≊';
    }

    @Override
    public @NotNull String getMenuName() {
        return "item_select";
    }

    @FunctionalInterface
    public interface ItemSelectFunction {
        void select(ListingData listingData);
    }
}
