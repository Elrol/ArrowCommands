package dev.elrol.arrow.commands.mixin;

import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.data.PokemonShopSaleData;
import dev.elrol.arrow.commands.data.ShopData;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.menus.shops.ItemSelectMenu;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.registries.ModEconomyRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigDecimal;
import java.util.UUID;

@Mixin(DisplayCaseBlockEntity.class)
public class DisplayCaseEntityMixin implements IDisplayShop {

    @Unique
    private boolean locked = false;

    @Unique
    private UUID owner;

    @Inject(method = "updateItem", at = @At("HEAD"), cancellable = true)
    public void arrowcommands$preventUpdate(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(arrowcommands$locked() && player instanceof ServerPlayerEntity serverPlayer) {
            arrowcommands$update(serverPlayer);

            if(arrowcommands$isShop()) {
                DisplayCaseBlockEntity target = (DisplayCaseBlockEntity)(Object)this;
                BlockPos pos = target.getPos();

                PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(arrowcommands$getOwner());
                PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                ShopData shop = commandData.playerShopData.getShop(pos);

                if(shop != null) {
                    //ToDo finish setting up functions for result of the ItemSelectMenu
                    ItemSelectMenu menu = (ItemSelectMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_select", serverPlayer);
                    menu.open(shop.getListing(serverPlayer.getServerWorld()), true, (listingData -> {
                        if(shop.getType().equals(ShopSaleDataTypes.POKEMON_SHOP)) {
                            PokemonShopSaleData pokeSaleData = (PokemonShopSaleData) shop.saleData;

                            IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
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
                        } else {
                            if(shop.getIsSelling().asBoolean()) {
                                //ToDo handle selling items
                            } else {
                                //ToDo handle buying items
                            }
                        }
                    }), () -> {

                    });
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
