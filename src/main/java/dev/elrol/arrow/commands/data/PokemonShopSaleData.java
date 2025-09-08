package dev.elrol.arrow.commands.data;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PokemonShopSaleData implements ShopSaleData {

    public static final MapCodec<PokemonShopSaleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(data -> data.owner.toString()),
            PokemonProperties.getCODEC().optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties)),
            Pokemon.getCODEC().optionalFieldOf("pokemon").forGetter(data -> Optional.ofNullable(data.pokemon)),
            Codec.INT.fieldOf("slot").forGetter(data -> data.slot)
    ).apply(instance, (owner, properties, pokemon, slot) -> {
        PokemonShopSaleData data = new PokemonShopSaleData(UUID.fromString(owner));
        properties.ifPresent(props -> data.properties = props.copy());
        pokemon.ifPresent(data::setPokemon);
        data.slot = slot;
        return data;
    }));

    PokemonProperties properties;
    Pokemon pokemon;
    public int slot;
    final UUID owner;
    public int amount = 1;
    public int box = -1;

    public PokemonShopSaleData(UUID owner) {
        this.owner = owner;
    }

    public void setPokemon(@Nullable Pokemon pokemon) {
        //if(pokemon == null) properties = null;
        //else properties = pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);
        this.pokemon = pokemon;
    }

    @NonNull
    public Optional<Pokemon> getPokemon() {
        return Optional.ofNullable(pokemon);
    }

    public ItemStack getPokemonItem() {
        Pokemon pokemon = getPokemon().orElse(null);
        if(pokemon == null) {
            ItemStack stack = new ItemStack(CobblemonItems.POKE_BALL, 1);
            stack.set(DataComponentTypes.CUSTOM_NAME, ModTranslations.info("for_sale").formatted(Formatting.RED));
            return stack;
        }
        return CobblemonUtils.makePokeStatItemStack(pokemon);
    }

    @Override
    public @NotNull ShopSaleData.Type<?> getType() {
        return ShopSaleDataTypes.POKEMON_SHOP;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return getPokemonItem();
    }

    @Override
    public @NonNull UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isShopOpen() {
        return properties != null || pokemon != null;
    }

    public void givePokemon(ServerPlayerEntity player) {
        getPokemon().ifPresent(pokemon -> {
            CobblemonUtils.givePokemon(player, pokemon);
            setPokemon(null);
        });
    }
}
