package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface ShopSaleData {

    Codec<Type<?>> shopSaleDataTypeCodec = Type.REGISTRY.getCodec();
    Codec<ShopSaleData> CODEC = shopSaleDataTypeCodec.dispatch("type", ShopSaleData::getType, Type::codec);

    ShopSaleData.Type<?> getType();
    @NonNull
    ItemStack getDisplayItem();

    @NonNull
    UUID getOwner();

    boolean isShopOpen();

    record Type<T extends ShopSaleData> (MapCodec<T> codec) {
        public static final Registry<Type<?>> REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of("arrow", "shop_sale_types")), Lifecycle.stable());
    }

}
