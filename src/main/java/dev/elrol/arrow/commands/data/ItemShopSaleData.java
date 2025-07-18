package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemShopSaleData implements ShopSaleData {

    public static final MapCodec<ItemShopSaleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("item").forGetter(data -> Optional.ofNullable(data.item)),
            BlockPos.CODEC.listOf().fieldOf("stock").forGetter(data -> data.stock),
            Codec.INT.fieldOf("amount").forGetter(data -> data.amount)
    ).apply(instance, (item, stock, amount) -> {
        ItemShopSaleData data = new ItemShopSaleData();
        item.ifPresent(data::setItem);
        data.stock.addAll(stock);
        data.amount = amount;
        return data;
    }));

    ItemStack item = null;
    List<BlockPos> stock = new ArrayList<>();
    int amount = 1;

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItemStack() {
        if(item == null) return ItemStack.EMPTY;
        return item;
    }

    public void addStock(BlockPos pos) {
        if(stock.contains(pos)) return;
        stock.add(pos);
    }
    public List<BlockPos> getStock() { return stock; }

    @Override
    public @NotNull ShopSaleDataType<?> getType() {
        return ShopSaleDataTypes.ITEM_SHOP;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return getItemStack();
    }
}
