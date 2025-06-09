package dev.elrol.arrow.commands.data;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.codecs.ArrowCodecs;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.libs.CobblemonUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DaycareData {

    public static final Codec<DaycareData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ArrowCodecs.DATE_TIME_CODEC.optionalFieldOf("eggLaid").forGetter(data -> Optional.ofNullable(data.eggLaid)),
                Codec.INT.fieldOf("timeLeft").forGetter(data -> -1),
                Pokemon.getCODEC().optionalFieldOf("egg").forGetter(data -> Optional.ofNullable(data.egg)),
                Codec.INT.fieldOf("slot1").forGetter(data -> data.slot1),
                Codec.INT.fieldOf("slot2").forGetter(data -> data.slot2)
        ).apply(instance, (eggLaid, timeLeft, egg, slot1, slot2) -> {
            DaycareData data = new DaycareData();
            data.eggLaid = eggLaid.orElse(LocalDateTime.MIN);
            data.egg = egg.orElse(null);
            data.slot1 = slot1;
            data.slot2 = slot2;
            return data;
        }));
    }

    @Nullable
    Pokemon egg = null;
    //private int timeLeft = 0;
    public int slot1 = -1;
    public int slot2 = -1;
    public LocalDateTime eggLaid = LocalDateTime.MIN;

    public void setEgg(PokemonProperties eggPokemon) {
        this.egg = eggPokemon.create();
        eggLaid = LocalDateTime.now();
    }

    public void hatchEgg(ServerPlayerEntity player) {
        if(egg == null) return;
        CobblemonUtils.givePokemon(player, egg);
        egg = null;
    }

    public @Nullable Pokemon getEgg() {
        return egg;
    }

    public boolean isBreeding() {
        return egg != null;
    }
    public boolean isReadyToHatch() {
        return getTime() <= 0 && isBreeding();
    }

    public long getTime(){
        float hatchTime = ArrowCommands.CONFIG.daycareSettings.minutesToHatchEgg;
        long sec = (long)(hatchTime * 60L);
        LocalDateTime timeTillHatch = eggLaid.plusSeconds(sec);
        LocalDateTime now = LocalDateTime.now();
        if(timeTillHatch.isAfter(now)) {
            return now.until(timeTillHatch, ChronoUnit.SECONDS);
        } else {
            return -1;
        }
    }

    public void clearSlots() {
        slot1 = -1;
        slot2 = -1;
    }
}
