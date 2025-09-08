package dev.elrol.arrow.commands.mixin;

import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.data.*;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.libs.BlockUtils;
import dev.elrol.arrow.commands.libs.InventoryUtils;
import dev.elrol.arrow.commands.menus.shops.ItemSelectMenu;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.data.ArrowPlayerData;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.ModUtils;
import dev.elrol.arrow.registries.ModEconomyRegistry;
import net.luckperms.api.util.Tristate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Mixin(DisplayCaseBlockEntity.class)
public class DisplayCaseEntityMixin implements IDisplayShop {

    @Unique
    private boolean locked = false;

    @Unique
    private UUID owner;

    @Inject(method = "canExtract", at = @At("HEAD"), cancellable = true)
    public void arrowcommands$canExtract(int slot, ItemStack stack, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(arrowcommands$locked()) cir.setReturnValue(false);
    }

    @Inject(method = "updateItem", at = @At("HEAD"), cancellable = true)
    public void arrowcommands$preventUpdate(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(arrowcommands$locked() && player instanceof ServerPlayerEntity serverPlayer) {
            arrowcommands$update(serverPlayer);

            if(arrowcommands$isShop()) {
                DisplayCaseBlockEntity target = (DisplayCaseBlockEntity)(Object)this;
                BlockPos pos = target.getPos();

                ArrowPlayerData ownerData = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(arrowcommands$getOwner());
                PlayerDataCommands ownerCommandData = ownerData.get(new PlayerDataCommands());
                ShopData shop = ownerCommandData.playerShopData.getShop(pos);

                ArrowPlayerData playerData = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
                PlayerDataCommands playerCommandData = playerData.get(new PlayerDataCommands());

                if(shop != null && shop.isShopOpen()) {
                    //Todo finish setting up functions for result of the ItemSelectMenu
                    ItemSelectMenu menu = (ItemSelectMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_select", serverPlayer);
                    ListingData listing = shop.getListing(serverPlayer);
                    playerCommandData.shoppingData.currentCart = listing;
                    playerData.put(playerCommandData);

                    if(shop.saleData instanceof PokemonShopSaleData pokeSaleData) {
                        listing.setMinUnits(1);
                        listing.setMaxUnits(pokeSaleData.amount);
                    }
                    if(shop.getIsSelling().equals(Tristate.FALSE)) {
                        listing.setSelling(false);
                    }

                    UUID ownerUUID = arrowcommands$getOwner();
                    if(ownerUUID != null) menu.setShopOwner(ownerUUID);

                    menu.open(listing, true, true, (listingData -> {
                        IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
                        int amount = listingData.getUnits();

                        if(shop.getType().equals(ShopSaleDataTypes.POKEMON_SHOP)) {
                            PokemonShopSaleData pokeSaleData = (PokemonShopSaleData) shop.saleData;

                            int total = listingData.getTotalPrice();
                            Currency cur = econRegistry.getPrimary();

                            if(shop.getIsSelling().asBoolean()) {
                                econRegistry.withdraw(serverPlayer, BigDecimal.valueOf(total), cur);
                                pokeSaleData.givePokemon(serverPlayer);
                            } else {
                                //ToDo handle buying pokemon
                                econRegistry.withdraw(serverPlayer, BigDecimal.valueOf(total), econRegistry.getPrimary());
                                pokeSaleData.givePokemon(serverPlayer);
                            }
                            shop.saleData = pokeSaleData;
                        } else {
                            ItemShopSaleData itemSaleData = (ItemShopSaleData) shop.saleData;
                            World world = player.getWorld();
                            Inventory inventory = InventoryUtils.getInventoryAt(world, itemSaleData.getStock());
                            ItemStack item = itemSaleData.getItemStack();

                            if(shop.getIsSelling().asBoolean()) {
                                //ToDo handle selling items
                                assert inventory != null;
                                if(InventoryUtils.getItemCount(inventory, item) >= (item.getCount() * amount)) {
                                    InventoryUtils.removeItems(inventory, item, amount);

                                    econRegistry.withdraw(serverPlayer, BigDecimal.valueOf(listingData.getTotalPrice()));
                                    econRegistry.deposit(arrowcommands$getOwner(), BigDecimal.valueOf(listingData.getTotalPrice()));

                                    for(int i = 0; i < amount; ++i) {
                                        serverPlayer.getInventory().offerOrDrop(item.copy());
                                    }

                                    ServerPlayerEntity owner = ModUtils.getPlayer(arrowcommands$getOwner());
                                    String total = econRegistry.formatAmount(listingData.getTotalPrice());
                                    if(owner != null) {
                                        owner.sendMessage(ModTranslations.msg("owner_item_sold", player.getName(), item.getName().getString() + "(" + item.getCount() + ")", amount, total), false);
                                    }
                                    player.sendMessage(ModTranslations.msg("player_item_bought", item.getName().getString() + "(" + item.getCount() + ")", amount, total), false);
                                }
                            } else {
                                //ToDo handle buying items
                                int totalAmount = item.getCount() * amount;
                                if(InventoryUtils.getItemCount(serverPlayer.getInventory(), item) >= totalAmount) {
                                    ServerPlayerEntity owner = ModUtils.getPlayer(arrowcommands$getOwner());

                                    InventoryUtils.removeItems(serverPlayer.getInventory(), item, amount);

                                    econRegistry.withdraw(arrowcommands$getOwner(), BigDecimal.valueOf(listingData.getTotalPrice()));
                                    econRegistry.deposit(serverPlayer, BigDecimal.valueOf(listingData.getTotalPrice()));

                                    assert inventory != null;
                                    InventoryUtils.addItems(inventory, item, amount);

                                    String total = econRegistry.formatAmount(listingData.getTotalPrice());
                                    if(owner != null) {
                                        owner.sendMessage(ModTranslations.msg("owner_item_bought", player.getName(), item.getName().getString() + "(" + item.getCount() + ")", amount, total), false);
                                    }
                                    player.sendMessage(ModTranslations.msg("player_item_sold", item.getName().getString() + "(" + item.getCount() + ")", amount, total), false);
                                }
                            }
                            shop.saleData = itemSaleData;
                        }
                        ownerCommandData.playerShopData.addShop(pos, shop);
                        ownerData.put(ownerCommandData);
                        if(!shop.isShopOpen()) {
                            target.setStack(0, ItemStack.EMPTY);
                        }
                        menu.close();
                    }), menu::close);
                }
            }
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void arrowcommands$writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.putBoolean("arrowcommands$locked", locked);
        if(owner != null) nbt.putString("arrowcommands$owner", owner.toString());
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void arrowcommands$readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        locked = nbt.getBoolean("arrowcommands$locked");
        String uuid = nbt.getString("arrowcommands$owner");
        if(uuid != null) {
            arrowcommands$setOwner(UUID.fromString(uuid));
        }
    }

    @Override
    public void arrowcommands$update(ServerPlayerEntity player) {
        DisplayCaseBlockEntity target = (DisplayCaseBlockEntity)(Object) this;
        target.markDirty();
        BlockState state = player.getServerWorld().getBlockState(target.getPos());
        player.getServerWorld().updateListeners(target.getPos(), state, state, 0);
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(target.getPos(), state));
        player.getInventory().updateItems();
    }

    @Override
    public void arrowcommands$lock() {
        locked = true;
    }

    @Override
    public void arrowcommands$unlock() {
        locked = false;
    }

    @Override
    public boolean arrowcommands$locked() {
        return locked;
    }

    @Override
    public UUID arrowcommands$getOwner() {
        return owner;
    }

    @Override
    public void arrowcommands$setOwner(UUID uuid) {
        owner = uuid;
    }

    @Override
    public boolean arrowcommands$isShop() {
        return owner != null;
    }
}
