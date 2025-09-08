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
                Codec.INT.fieldOf("units").forGetter(data -> data.units),
                Codec.INT.optionalFieldOf("minUnits", 0).forGetter(ListingData::getMinUnits),
                Codec.INT.optionalFieldOf("maxUnits", 0).forGetter(ListingData::getMaxUnits),
                Codec.BOOL.optionalFieldOf("isSelling", false).forGetter(ListingData::isSelling)
        ).apply(instance, (item, pricePerUnit, units, minUnits, maxUnits, isSelling) -> {
            ListingData data = new ListingData(item, pricePerUnit, units);
            data.setMinUnits(minUnits);
            data.setMaxUnits(maxUnits);
            data.setSelling(isSelling);
            return data;
        }));
    }

    ItemStack item;
    int pricePerUnit;
    int units;
    int minUnits = 0;
    int maxUnits = -1;
    int maxStackSize;
    boolean isSelling = true;

    public ListingData(ItemStack item, int pricePerUnit, int units) {
        this.item = item;
        this.pricePerUnit = pricePerUnit;
        this.units = units;
        maxStackSize = item.getMaxCount();
    }

    public ListingData() {
        this.item = new ItemStack(Items.BEDROCK);
        this.pricePerUnit = 0;
        this.units = 0;
        maxStackSize = item.getMaxCount();
    }

    public void changeUnits(int amount) {
        units += amount;
        checkUnits();
    }

    public void checkUnits() {
        if(units < minUnits) units = minUnits;
        if(units > maxUnits && maxUnits >= 0) units = maxUnits;
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

    public void setMinUnits(int minUnits) {
        this.minUnits = minUnits;
        this.units = minUnits;
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

    /**
     * @return If this listing is selling to the player
     */
    public boolean isSelling() {
        return isSelling;
    }

    public void setSelling(boolean isSelling) {
        this.isSelling = isSelling;
    }

    public boolean isEmpty() {
        return item.getItem().equals(Items.BEDROCK) || pricePerUnit <= 0;
    }

    public int getMaxUnits() {
        return maxUnits;
    }
    public int getMinUnits() {
        return minUnits;
    }
}
