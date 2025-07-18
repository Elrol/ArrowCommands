package dev.elrol.arrow.commands.data;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.libs.CobblemonUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PokemonShopSaleData implements ShopSaleData {

    public static final MapCodec<PokemonShopSaleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Pokemon.getCODEC().optionalFieldOf("pokemon").forGetter(data -> Optional.ofNullable(data.pokemon))
    ).apply(instance, pokemon -> {
        PokemonShopSaleData data = new PokemonShopSaleData();
        pokemon.ifPresent(p -> data.pokemon = p);
        return data;
    }));

    Pokemon pokemon;

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public ItemStack getPokemonItem() {
        return CobblemonUtils.makePokeStatItemStack(pokemon);
    }

    @Override
    public @NotNull ShopSaleDataType<?> getType() {
        return ShopSaleDataTypes.POKEMON_SHOP;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return pokemon == null ? new ItemStack(CobblemonItems.POKE_BALL, 1) : getPokemonItem();
    }
}
