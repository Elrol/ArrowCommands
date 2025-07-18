package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerShopData {

    public static final Codec<PlayerShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ShopData.CODEC).fieldOf("shops").forGetter(data -> data.shops),
            TempShopData.CODEC.optionalFieldOf("tempShop").forGetter(data -> Optional.ofNullable(data.tempShop))
    ).apply(instance, (shops, tempShop) -> {
        PlayerShopData data = new PlayerShopData();
        data.shops.putAll(shops);
        tempShop.ifPresent(temp -> data.tempShop = temp);
        return data;
    }));

    private final Map<String, ShopData> shops = new HashMap<>();
    public TempShopData tempShop = null;

    public void addShop(BlockPos pos, ShopData shop) {
        shops.put(pos.toShortString(), shop);
    }

    public void removeShop(BlockPos pos) {
        shops.remove(pos.toShortString());
    }

    @Nullable
    public ShopData getShop(BlockPos pos) {
        return shops.get(pos.toShortString());
    }

    public Collection<ShopData> getShops() {
        return shops.values();
    }
}
