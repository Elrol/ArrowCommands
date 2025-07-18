package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class TempShopData {

    public static final Codec<TempShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShopData.CODEC.fieldOf("shop").forGetter(data -> data.shop),
            Codec.STRING.fieldOf("stage").forGetter(data -> data.stage.name())
    ).apply(instance, (shop, stage) -> {
       TempShopData data = new TempShopData(shop);
       ShopStage tempStage;
       try {
            tempStage = ShopStage.valueOf(stage);
       } catch(IllegalArgumentException e) {
           tempStage = ShopStage.none;
       }

        data.stage = tempStage;
       return data;
    }));

    public boolean isValid(BlockPos pos) {
        return stage.equals(TempShopData.ShopStage.saleData)
                && getShopType().equals(ShopSaleDataTypes.ITEM_SHOP)
                && hasDisplayCase()
                && shop.displayCase != null
                && shop.displayCase.equals(pos);
    }

    public enum ShopStage {
        none,
        displayCase,
        saleData,
        price,
        stock,
        cancel
    }

    @NonNull
    final public ShopData shop;
    ShopStage stage = ShopStage.none;

    public TempShopData(@NonNull final UUID owner) {
        shop = new ShopData(owner);
    }

    public TempShopData(@NonNull final ShopData shop) {
        this.shop = shop;
    }

    public void resetStage()                                { setStage(ShopStage.none); }
    public void setStage(@NonNull final ShopStage stage)    { this.stage = stage; }

    @NonNull public ShopStage getStage()                    { return stage; }
    public boolean hasPrice()                               { return shop.getPrice() > 0; }
    public ShopSaleData.ShopSaleDataType<?> getShopType()   { return shop.saleData.getType(); }
    public TriState getIsSelling()                          { return shop.getIsSelling(); }
    public boolean hasDisplayItem()                         { return !shop.saleData.getDisplayItem().isEmpty(); }
    public boolean hasDisplayCase()                         { return shop.getDisplayCase() != null; }
}
