package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ServerShopItem {
    public static final Codec<ServerShopItem> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.VALIDATED_CODEC.fieldOf("item").forGetter(data -> data.item),
                Codec.INT.fieldOf("cost").forGetter(data -> data.cost)
        ).apply(instance, ServerShopItem::new));
    }

    public ItemStack item;
    public int cost;

    public ServerShopItem() {
        item = new ItemStack(Items.DIAMOND);
        cost = 100000;
    }

    public ServerShopItem(ItemStack item, int cost) {
        this.item = item;
        this.cost = cost;
    }
}
