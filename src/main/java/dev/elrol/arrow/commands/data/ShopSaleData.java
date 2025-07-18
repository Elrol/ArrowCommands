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

public interface ShopSaleData {

    Codec<ShopSaleData> CODEC = ShopSaleDataType.REGISTRY.getCodec()
            .dispatch("type", ShopSaleData::getType, ShopSaleDataType::codec);

    @NonNull
    ShopSaleDataType<?> getType();
    @NonNull
    ItemStack getDisplayItem();

    record ShopSaleDataType<T extends ShopSaleData>(MapCodec<T> codec) {
        public static final Registry<ShopSaleDataType<?>> REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of("arrow", "shop_sale_types")), Lifecycle.stable());
    }
}
