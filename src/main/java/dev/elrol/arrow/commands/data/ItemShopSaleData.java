package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemShopSaleData implements ShopSaleData {

    public static final MapCodec<ItemShopSaleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(data -> data.owner.toString()),
            ItemStack.CODEC.optionalFieldOf("item", null).forGetter(data -> data.item),
            BlockPos.CODEC.optionalFieldOf("stock", null).forGetter(data -> data.stock)
    ).apply(instance, (owner, item, stock) -> {
        ItemShopSaleData data = new ItemShopSaleData(UUID.fromString(owner));
        data.setItem(item);
        data.stock = stock;
        return data;
    }));

    ItemStack item = null;
    BlockPos stock = null;
    final UUID owner;

    public ItemShopSaleData(UUID owner) {
        this.owner = owner;
    }

    public void setItem(ItemStack item) {
        this.item = item.isEmpty() ? null : item.copy();
    }

    public ItemStack getItemStack() {
        if(item == null) return ItemStack.EMPTY;
        return item.copy();
    }

    public int getAmount() {
        return item.getCount();
    }

    public void setStock(BlockPos pos) {
        stock = pos;
    }

    public BlockPos getStock() { return stock; }

    @Override
    public @NotNull ShopSaleData.Type<?> getType() {
        return ShopSaleDataTypes.ITEM_SHOP;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return getItemStack();
    }

    @Override
    public @NonNull UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isShopOpen() {
        return !item.isEmpty();
    }
}
