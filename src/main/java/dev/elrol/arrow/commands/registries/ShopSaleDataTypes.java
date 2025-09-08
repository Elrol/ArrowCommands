package dev.elrol.arrow.commands.registries;

import dev.elrol.arrow.commands.data.ItemShopSaleData;
import dev.elrol.arrow.commands.data.PokemonShopSaleData;
import dev.elrol.arrow.commands.data.ShopSaleData;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ShopSaleDataTypes {

    public static final ShopSaleData.Type<PokemonShopSaleData> POKEMON_SHOP = register("pokemon", new ShopSaleData.Type<>(PokemonShopSaleData.CODEC));
    public static final ShopSaleData.Type<ItemShopSaleData> ITEM_SHOP = register("item", new ShopSaleData.Type<>(ItemShopSaleData.CODEC));

    public static <T extends ShopSaleData> ShopSaleData.Type<T> register(String id, ShopSaleData.Type<T> type) {
        return Registry.register(ShopSaleData.Type.REGISTRY, Identifier.of("arrow", id), type);
    }

    public static void init() {}
}
