package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.codecs.ArrowCodecs;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import net.luckperms.api.util.Tristate;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ShopData {

    public static final Codec<ShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(ShopData::getOwner),
            ArrowCodecs.BLOCK_POS_CODEC.optionalFieldOf("displayCase").forGetter(data -> Optional.ofNullable(data.getDisplayCase())),
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
        if(saleData instanceof ItemShopSaleData itemSaleData) {
            data.saleData = itemSaleData;
        } else if (saleData instanceof PokemonShopSaleData pokeSaleData) {
            data.saleData = pokeSaleData;
        } else {
            ArrowCommands.LOGGER.debug(saleData.toString());
            data.saleData = null;
        }
        data.setPrice(price);
        data.setIsSelling(switch (isSelling) {
            case -1 -> Tristate.FALSE;
            case 1 -> Tristate.TRUE;
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

    public int getMaxUnits(ServerWorld world) {
        if(saleData.getType().equals(ShopSaleDataTypes.POKEMON_SHOP)) return 1;

        ItemShopSaleData itemSaleData = (ItemShopSaleData) saleData;
        List<BlockPos> invalidLocations = new ArrayList<>();

        AtomicInteger total = new AtomicInteger();
        ItemStack target = itemSaleData.item.copyWithCount(1);

        itemSaleData.stock.forEach((pos -> {
            BlockEntity entity = world.getBlockEntity(pos);
            if(entity instanceof ChestBlockEntity || entity instanceof BarrelBlockEntity) {
                LockableContainerBlockEntity storage = (LockableContainerBlockEntity) entity;
                for(int i = 0; i < 27; i++) {
                    ItemStack slot = storage.getStack(i);
                    if(slot.copyWithCount(1).equals(target)) {
                        total.addAndGet(slot.getCount());
                    }
                }
            } else {
                invalidLocations.add(pos);
            }
        }));

        itemSaleData.stock.removeAll(invalidLocations);
        saleData = itemSaleData;

        return Math.floorDiv(total.get(), itemSaleData.amount);
    }

    public ListingData getListing(ServerWorld world) {
        ListingData listing = new ListingData(saleData.getDisplayItem(), price, 1);
        listing.setMaxUnits(getMaxUnits(world));
        return listing;
    }

    public ShopSaleData.Type<?> getType() { return saleData.getType(); }

    public boolean isShopOpen() {
        boolean isItemShop = getType().equals(ShopSaleDataTypes.ITEM_SHOP);
        IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        if(isItemShop) {
            if(isSelling.asBoolean()) {
                //Selling items
            } else {
                //Buying items
            }
        } else {
            if(isSelling.asBoolean()) {
                //Selling Pokémon
                return saleData.isShopOpen();
            } else {
                //Buying Pokémon
                return econRegistry.canAfford(owner, BigDecimal.valueOf(getPrice()));
            }
        }
        return true;
    }
}
