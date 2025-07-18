package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ListingData {

    public static final Codec<ListingData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(data -> data.item),
                Codec.INT.fieldOf("pricePerUnit").forGetter(data -> data.pricePerUnit),
                Codec.INT.fieldOf("units").forGetter(data -> data.units)
        ).apply(instance, ListingData::new));
    }

    ItemStack item;
    int pricePerUnit;
    int units;
    int maxUnits = 0;
    int maxStackSize;

    public ListingData(ItemStack item, int pricePerUnit, int units) {
        this.item = item;
        this.pricePerUnit = pricePerUnit;
        this.units = units;
        maxStackSize = item.getMaxCount();
    }

    public ListingData() {
        this.item = new ItemStack(Items.BEDROCK);
        this.pricePerUnit = 0;
        this.units = 1;
        maxStackSize = item.getMaxCount();
    }

    public void changeUnits(int amount) {
        units += amount;
        checkUnits();
    }

    public void checkUnits() {
        if(units < 1) units = 1;
        if(units > maxUnits && maxUnits > 0) units = maxUnits;
    }

    public int getPricePerUnit() {
        return pricePerUnit;
    }

    public int getTotalPrice() {
        return pricePerUnit * units;
    }

    public String getPriceString() {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        return economyRegistry.formatAmount(getTotalPrice());
    }

    public int getUnits() {
        return units;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public void setMaxStackSize(int maxStack) {
        this.maxStackSize = maxStack;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public boolean isEmpty() {
        return item.getItem().equals(Items.BEDROCK) || pricePerUnit <= 0;
    }
}
