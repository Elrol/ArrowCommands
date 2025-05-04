package dev.elrol.arrow.commands.data;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.libs.CobblemonUtils;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DaycareData {

    public static final Codec<DaycareData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("timeLeft").forGetter(data -> data.timeLeft),
            Pokemon.getCODEC().optionalFieldOf("egg").forGetter(data -> Optional.ofNullable(data.egg)),
            Codec.INT.fieldOf("slot1").forGetter(data -> data.slot1),
            Codec.INT.fieldOf("slot2").forGetter(data -> data.slot2)
        ).apply(instance, (timeLeft, egg, slot1, slot2) -> {
            DaycareData data = new DaycareData();
            data.timeLeft = timeLeft;
            data.egg = egg.orElse(null);
            data.slot1 = slot1;
            data.slot2 = slot2;
            return data;
        }));
    }

    @Nullable
    Pokemon egg = null;
    private int timeLeft = 0;
    public int slot1 = -1;
    public int slot2 = -1;

    public void setEgg(PokemonProperties eggPokemon, ServerPlayerEntity player) {
        this.egg = eggPokemon.create();
        ArrowCore.INSTANCE.getPlayerDataRegistry().save(player.getUuid());
    }

    public void hatchEgg(ServerPlayerEntity player) {
        if(egg == null) return;
        CobblemonUtils.givePokemon(player, egg);
        egg = null;
    }

    public @Nullable Pokemon getEgg() {
        return egg;
    }

    public TriState tickTime() {
        if(timeLeft < 0) return TriState.DEFAULT;

        timeLeft--;

        if(timeLeft == 0) {
            timeLeft = -1;
            return TriState.TRUE;
        }
        return TriState.FALSE;
    }

    public void setTime(int seconds) {
        timeLeft = seconds;
    }

    public boolean isBreeding() {
        return egg != null;
    }
    public boolean isReadyToHatch() {
        return timeLeft == -1 && isBreeding();
    }
    public int getTime(){ return timeLeft; }
}
