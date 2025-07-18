package dev.elrol.arrow.commands.registries;

import dev.elrol.arrow.commands.data.ItemShopSaleData;
import dev.elrol.arrow.commands.data.PokemonShopSaleData;
import dev.elrol.arrow.commands.data.ShopSaleData;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ShopSaleDataTypes {

    public static final ShopSaleData.ShopSaleDataType<PokemonShopSaleData> POKEMON_SHOP = register("pokemon", new ShopSaleData.ShopSaleDataType<>(PokemonShopSaleData.CODEC));
    public static final ShopSaleData.ShopSaleDataType<ItemShopSaleData> ITEM_SHOP = register("item", new ShopSaleData.ShopSaleDataType<>(ItemShopSaleData.CODEC));

    public static <T extends ShopSaleData> ShopSaleData.ShopSaleDataType<T> register(String id, ShopSaleData.ShopSaleDataType<T> type) {
        return Registry.register(ShopSaleData.ShopSaleDataType.REGISTRY, Identifier.of("arrow", id), type);
    }

}
