package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.libs.BlockUtils;
import dev.elrol.arrow.commands.libs.InventoryUtils;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import net.luckperms.api.util.Tristate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class ShopData {

    public static final Codec<ShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(ShopData::getOwner),
            BlockPos.CODEC.optionalFieldOf("displayCase").forGetter(data -> Optional.ofNullable(data.getDisplayCase())),
            ShopSaleData.CODEC.fieldOf("saleData").forGetter(data -> data.saleData),
            Codec.INT.fieldOf("price").forGetter(ShopData::getPrice),
            Codec.INT.fieldOf("isSelling").forGetter(data -> switch (data.isSelling) {
                case TRUE -> 1;
                case FALSE -> -1;
                default -> 0;
            })
    ).apply(instance, (owner, displayCase, saleData, price, isSelling) -> {
        ShopData data = new ShopData(owner);
        displayCase.ifPresent(data::setDisplayCase);
        data.saleData = saleData;
        data.setPrice(price);
        data.setIsSelling(switch (isSelling) {
            case 1 -> Tristate.TRUE;
            case -1 -> Tristate.FALSE;
            default -> Tristate.UNDEFINED;
        });

        return data;
    }));

    UUID owner;
    BlockPos displayCase = new BlockPos(0,0,0);
    public ShopSaleData saleData;
    int price = -1;
    Tristate isSelling = Tristate.UNDEFINED;

    public ShopData(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setDisplayCase(BlockPos displayCase) {
        this.displayCase = displayCase;
    }

    @Nullable
    public BlockPos getDisplayCase() {
        return displayCase.equals(new BlockPos(0,0,0)) ? null : displayCase;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * @return If the shop selling to the player
     */
    public Tristate getIsSelling() {
        //ToDo change this to allow buying of pokemon
        if(saleData.getType().equals(ShopSaleDataTypes.POKEMON_SHOP)) return Tristate.TRUE;
        return isSelling;
    }

    public void setIsSelling(Tristate isSelling) {
        this.isSelling = isSelling;
    }

    public String getFormatedPrice() {
        return ArrowCore.INSTANCE.getEconomyRegistry().formatAmount(price);
    }

    public int getMaxUnits(ServerPlayerEntity player) {
        if(saleData.getType().equals(ShopSaleDataTypes.POKEMON_SHOP)) {
            PokemonShopSaleData pokemonShopSaleData = (PokemonShopSaleData) saleData;
            return pokemonShopSaleData.amount;
        } else {
            ItemShopSaleData itemSaleData = (ItemShopSaleData) saleData;
            BlockPos pos = itemSaleData.stock;
            MinecraftServer server = player.getServer();
            assert server != null;
            ServerWorld world = server.getOverworld();
            Inventory stockInventory = InventoryUtils.getInventoryAt(world, pos);
            if(stockInventory == null) return 0;

            if(isSelling.equals(Tristate.TRUE)) {
                return Math.floorDiv(
                        InventoryUtils.getItemCount(stockInventory, itemSaleData.getItemStack()),
                        itemSaleData.getAmount());
            } else if(isSelling.equals(Tristate.FALSE)) {
                return Math.floorDiv(
                        Math.min(
                                InventoryUtils.getItemCount(player.getInventory(), itemSaleData.getItemStack()),
                                InventoryUtils.spaceLeft(stockInventory, itemSaleData.getItemStack())),
                        itemSaleData.getAmount());
            }
        }
        return 0;
    }

    public ListingData getListing(ServerPlayerEntity player) {
        ListingData listing = new ListingData(saleData.getDisplayItem(), price, 0);
        listing.setMaxUnits(getMaxUnits(player));
        return listing;
    }

    public ShopSaleData.Type<?> getType() { return saleData.getType(); }

    public boolean isShopOpen() {
        return saleData.isShopOpen();
    }
}
