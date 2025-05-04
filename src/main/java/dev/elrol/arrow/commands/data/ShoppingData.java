package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingData {

    public static final Codec<ShoppingData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("page").forGetter(data -> data.page),
            Codec.STRING.fieldOf("shop").forGetter(data -> data.shop),
            ListingData.CODEC.listOf().fieldOf("shoppingCart").forGetter(data -> data.shoppingCart),
            ListingData.CODEC.fieldOf("currentCart").forGetter(data -> data.currentCart)
        ).apply(instance, (page, shop, shoppingCart, currentCart) -> {
            ShoppingData data = new ShoppingData();
            data.page = page;
            data.shop = shop;
            data.shoppingCart = new ArrayList<>(shoppingCart);
            data.currentCart = currentCart;
            return data;
        }));
    }

    public int page = 0;
    public String shop = "";

    @Nonnull
    public List<ListingData> shoppingCart = new ArrayList<>();
    @Nonnull
    public ListingData currentCart = new ListingData();

    public void confirmCurrentCart() {
        if(currentCart.isEmpty()) return;
        shoppingCart.add(currentCart);
        currentCart = new ListingData();
    }

    public void cancelCurrentCart() {
        currentCart = new ListingData();
    }

    public void finalizePurchase(ServerPlayerEntity player) {
        if(shoppingCart.isEmpty()) return;
        ServerWorld world = player.getServerWorld();

        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        economyRegistry.withdraw(player.getUuid(), BigDecimal.valueOf(getTotalPrice()));

        for(ListingData listingData : shoppingCart) {
            int amount = listingData.getUnits();
            int max = listingData.getMaxStackSize();
            while(amount > 0) {
                int toTake = Math.min(amount, max);
                world.spawnEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), listingData.item.copyWithCount(toTake)));
                amount -= toTake;
            }
        }

        shoppingCart.clear();
    }

    public double getTotalPrice() {
        double total = 0;

        for(ListingData listingData : shoppingCart) {
            total += listingData.getTotalPrice();
        }

        return total;
    }
}
