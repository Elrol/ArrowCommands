package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ServerShopData {

    public static final Codec<ServerShopData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("shopID").forGetter(data -> data.shopID),
                TextCodecs.CODEC.fieldOf("name").forGetter(data -> data.name),
                Formatting.CODEC.fieldOf("color").forGetter(data -> data.color),
                ItemStack.VALIDATED_CODEC.fieldOf("shopIcon").forGetter(data -> data.shopIcon),
                ServerShopItem.CODEC.listOf().fieldOf("itemShop").forGetter(data -> data.itemShop)
        ).apply(instance, (shopID, name, color, shopIcon, itemShop) -> {
            ServerShopData data = new ServerShopData();
            data.shopID = shopID;
            data.name = name;
            data.color = color;
            data.shopIcon = shopIcon;
            data.itemShop = new ArrayList<>(itemShop);
            return data;
        }));
    }

    public String shopID = "example";
    public Text name = Text.literal("Example").formatted(Formatting.RED);
    public Formatting color = Formatting.GOLD;
    public ItemStack shopIcon = new ItemStack(Items.DIAMOND, 1);
    public List<ServerShopItem> itemShop = new ArrayList<>();

    public ServerShopData() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 1);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
        itemShop.add(new ServerShopItem(stack, 100000));
    }
}
