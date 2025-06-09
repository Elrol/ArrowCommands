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

    public ListingData(ItemStack item, int pricePerUnit, int units) {
        this.item = item;
        this.pricePerUnit = pricePerUnit;
        this.units = units;
    }

    public ListingData() {
        this.item = new ItemStack(Items.BEDROCK);
        this.pricePerUnit = 0;
        this.units = 0;
    }

    public void changeUnits(int amount) {
        units += amount;
        checkUnits();
    }

    public void checkUnits() {
        if(units < 0) units = 0;
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

    public int getMaxStackSize() {
        return item.getMaxCount();
    }

    public boolean isEmpty() {
        return item.getItem().equals(Items.BEDROCK) || pricePerUnit <= 0;
    }
}
