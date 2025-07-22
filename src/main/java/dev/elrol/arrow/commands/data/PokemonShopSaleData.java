package dev.elrol.arrow.commands.data;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.libs.CobblemonUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PokemonShopSaleData implements ShopSaleData {

    public static final MapCodec<PokemonShopSaleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(data -> data.owner.toString()),
            Pokemon.getCODEC().optionalFieldOf("pokemon").forGetter(data -> Optional.ofNullable(data.pokemon)),
            Codec.INT.fieldOf("slot").forGetter(data -> data.slot)
    ).apply(instance, (owner, pokemon, slot) -> {
        PokemonShopSaleData data = new PokemonShopSaleData(UUID.fromString(owner));
        pokemon.ifPresent(p -> data.pokemon = p);
        data.slot = slot;
        return data;
    }));

    Pokemon pokemon;
    public int slot;
    final UUID owner;

    public PokemonShopSaleData(UUID owner) {
        this.owner = owner;
    }

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
    public @NotNull ShopSaleData.Type<?> getType() {
        return ShopSaleDataTypes.POKEMON_SHOP;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return pokemon == null ? new ItemStack(CobblemonItems.POKE_BALL, 1) : getPokemonItem();
    }

    @Override
    public @NonNull UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isShopOpen() {
        return pokemon != null;
    }

    public void givePokemon(ServerPlayerEntity player) {
        if(pokemon != null) {
            CobblemonUtils.givePokemon(player, pokemon);
            setPokemon(null);
        }
    }
}
