package dev.elrol.arrow.commands.libs;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.abilities.PotentialAbilityType;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.*;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.libs.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DaycareUtils {

    private static Random rand = new Random();

    public static PokemonProperties breed(ServerPlayerEntity player, Pokemon firstPoke, Pokemon secondPoke) {
        rand = new Random();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ArrowCommands.LOGGER.warn("Species 1: {}", firstPoke.getSpecies().getName());
            ArrowCommands.LOGGER.warn("Species 2: {}", secondPoke.getSpecies().getName());
        }

        PokemonProperties egg = new PokemonProperties();
        Species species = PokemonSpecies.INSTANCE.random();

        while (!ArrowCommands.CONFIG.daycareSettings.dittoBreedingLegendaries && species.getStandardForm().getLabels().contains(CobblemonPokemonLabels.LEGENDARY)) {
            species = PokemonSpecies.INSTANCE.random();
        }

        boolean isDitto1 = CobblemonUtils.isDitto(firstPoke);
        boolean isDitto2 = CobblemonUtils.isDitto(secondPoke);

        // Set Species
        if(isDitto1 && !isDitto2 || secondPoke.getGender().equals(Gender.FEMALE)) species = secondPoke.getSpecies();
        if((isDitto2 && !isDitto1) || firstPoke.getGender().equals(Gender.FEMALE)) species = firstPoke.getSpecies();

        egg.setSpecies(getBabyPokemon(species).showdownId());

        // Set Shiny
        String t1 = firstPoke.getOriginalTrainer();
        String t2 = secondPoke.getOriginalTrainer();

        if(t1 != null && t2 != null) {
            boolean difTrainers = t1.equals(t2);
            boolean isShiny = ModUtils.temptFate(1.0f, 0, difTrainers ? 256 : 512);
            egg.setShiny(isShiny);
        }

        // Set IVs
        egg.setIvs(getEggStats(firstPoke, secondPoke));
        if(egg.getIvs() != null) {
            egg.getIvs().forEach((iv) -> { if(FabricLoader.getInstance().isDevelopmentEnvironment()) ArrowCommands.LOGGER.warn("New IVs:{} {}", iv.getKey().getDisplayName().getString(), iv.getValue()); });
        }

        // Set Gender
        if(species.getPossibleGenders().contains(Gender.GENDERLESS)) {
            egg.setGender(Gender.GENDERLESS);
        } else {
            float maleRatio = species.getMaleRatio();
            float selected = rand.nextFloat(0, 1.0F);

            if (selected < maleRatio) egg.setGender(Gender.MALE);
            else egg.setGender(Gender.FEMALE);
        }

        // Set Nature
        egg.setNature(getEggNature(firstPoke, secondPoke).getName().getPath());

        // Set Form
        FormData formData = getEggForm(firstPoke, secondPoke);
        if(formData != null) egg.setForm(formData.getName());

        // Set Ability
        Ability ability = getEggAbility(species,firstPoke, secondPoke);
        if(ability!= null) egg.setAbility(ability.getName());

        // Set Moves
        List<String> moveList = new ArrayList<>();
        if(egg.getMoves() != null) moveList = egg.getMoves();
        for(MoveTemplate moveTemplate : getEggMoves(species, firstPoke, secondPoke)) {
            moveList.add(moveTemplate.getName());
        }
        egg.setMoves(moveList);
        egg.setOriginalTrainer(player.getUuid().toString());
        egg.setOriginalTrainerType(OriginalTrainerType.PLAYER);

        egg.setPokeball(getEggBall(firstPoke, secondPoke).getName().getPath());

        // Set EVs
        egg.setEvs(EVs.createEmpty());

        egg.getCustomProperties().forEach(property -> ArrowCommands.LOGGER.warn(property.asString()));
        DataResult<JsonElement> json = PokemonProperties.getCODEC().encodeStart(JsonOps.INSTANCE, egg);

        JsonUtils.saveToJson(Constants.ARROW_DATA_DIR, "test.json", json.getOrThrow());
        return egg;
    }

    public static boolean canPokemonBreed(Pokemon pokemon1, Pokemon pokemon2) {
        final Set<EggGroup> eggGroups1 = pokemon1.getSpecies().getEggGroups();
        final Set<EggGroup> eggGroups2 = pokemon2.getSpecies().getEggGroups();

        final Gender gender1 = pokemon1.getGender();
        final Gender gender2 = pokemon2.getGender();

        // If either pokemon is unable to make eggs
        if(eggGroups1.contains(EggGroup.UNDISCOVERED) || eggGroups2.contains(EggGroup.UNDISCOVERED)) return false;

        // If either or both pokemon is a ditto
        if(CobblemonUtils.isDitto(pokemon1) || CobblemonUtils.isDitto(pokemon2)) {
            if(!ArrowCommands.CONFIG.daycareSettings.doubleDittoBreeding) {
                return !(CobblemonUtils.isDitto(pokemon1) && CobblemonUtils.isDitto(pokemon2));
            }
            return true;
        }

        // If the pokemon share an egg group
        if((gender1.equals(Gender.FEMALE) && gender2.equals(Gender.MALE)) || (gender1.equals(Gender.MALE) && gender2.equals(Gender.FEMALE))){
            for (EggGroup group : eggGroups2) {
                if (eggGroups1.contains(group)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static IVs getEggStats(Pokemon poke1, Pokemon poke2) {
        IVs ivs1 = poke1.getIvs();
        IVs ivs2 = poke2.getIvs();
        IVs ivs = IVs.createRandomIVs(0);
        boolean hasDestinyKnot = CobblemonUtils.hasDestinyKnot(poke1,poke2);

        List<Stats> validIVs = new ArrayList<>(Constants.IV_STATS);
        int ivsSelected = 0;

        Stats powerStat1 = getPowerStat(poke1);
        Stats powerStat2 = getPowerStat(poke2);

        if(powerStat1 != null && validIVs.contains(powerStat1)){
            if(FabricLoader.getInstance().isDevelopmentEnvironment())
                ArrowCommands.LOGGER.warn("Power(1) IV Selected: {}", powerStat1.getDisplayName());

            Integer iv = ivs1.get(powerStat1);
            if(iv != null) ivs.set(powerStat1, iv);
            validIVs.remove(powerStat1);
            ivsSelected++;
        }
        if(powerStat2 != null && validIVs.contains(powerStat2)){
            if(FabricLoader.getInstance().isDevelopmentEnvironment())
                ArrowCommands.LOGGER.warn("Power(2) IV Selected: {}", powerStat2.getDisplayName());

            Integer iv = ivs2.get(powerStat2);
            if(iv != null) ivs.set(powerStat2, iv);
            validIVs.remove(powerStat2);
            ivsSelected++;
        }

        for(int i = ivsSelected ; i < (hasDestinyKnot ? 5 : 3); i++) {
            if(FabricLoader.getInstance().isDevelopmentEnvironment())
                ArrowCommands.LOGGER.warn("ivSelected: {}", i);

            int index = rand.nextInt(0, validIVs.size());
            Stats selected = validIVs.get(index);

            Integer iv = rand.nextBoolean() ? ivs1.get(selected) : ivs2.get(selected);

            if(FabricLoader.getInstance().isDevelopmentEnvironment())
                ArrowCore.LOGGER.warn("Random IV Selected: {} ({})", selected.getDisplayName(), iv);

            if(iv != null) {
                ivs.set(selected, iv);

                if(FabricLoader.getInstance().isDevelopmentEnvironment())
                    ArrowCore.LOGGER.warn("{} IV set: {}", selected.getDisplayName(), ivs.get(selected));
            }
            validIVs.remove(selected);
            ivsSelected++;
        }
        if(FabricLoader.getInstance().isDevelopmentEnvironment())
            ArrowCore.LOGGER.warn("IVs Passed On: {}", ivsSelected);
        return ivs;
    }

    @Nullable
    private static Stats getPowerStat(final Pokemon pokemon) {
        final ItemStack item = pokemon.heldItem();
        if(item.isEmpty()) return null;

        final Item i = item.getItem();
        if(i == CobblemonItems.POWER_ANKLET) return Stats.SPEED;
        if(i == CobblemonItems.POWER_BAND) return Stats.SPECIAL_DEFENCE;
        if(i == CobblemonItems.POWER_BELT) return Stats.DEFENCE;
        if(i == CobblemonItems.POWER_BRACER) return Stats.ATTACK;
        if(i == CobblemonItems.POWER_LENS) return Stats.SPECIAL_ATTACK;
        if(i == CobblemonItems.POWER_WEIGHT) return Stats.HP;
        return null;
    }

    private static Nature getEggNature(Pokemon poke1, Pokemon poke2) {
        if(CobblemonUtils.isHoldingItem(poke1, CobblemonItems.EVERSTONE)) return poke1.getNature();
        if(CobblemonUtils.isHoldingItem(poke2, CobblemonItems.EVERSTONE)) return poke2.getNature();
        return Natures.INSTANCE.getRandomNature();

    }

    @Nullable
    private static FormData getEggForm(Pokemon poke1, Pokemon poke2) {
        if(CobblemonUtils.isHoldingItem(poke1, CobblemonItems.EVERSTONE)) return poke1.getForm();
        if(CobblemonUtils.isHoldingItem(poke2, CobblemonItems.EVERSTONE)) return poke2.getForm();
        return null;
    }

    private static PokeBall getEggBall(Pokemon poke1, Pokemon poke2) {
        if(poke1.getSpecies().equals(poke2.getSpecies())) return (rand.nextBoolean() ? poke1.getCaughtBall() : poke2.getCaughtBall());
        else if(CobblemonUtils.isDitto(poke1)) return poke2.getCaughtBall();
        else if(CobblemonUtils.isDitto(poke2)) return poke1.getCaughtBall();
        return PokeBalls.INSTANCE.getPOKE_BALL();
    }

    @Nullable
    private static Ability getEggAbility(Species species, Pokemon firstPoke, Pokemon secondPoke) {
        Pokemon parent = firstPoke;
        if(CobblemonUtils.isDitto(parent)) {
            if(CobblemonUtils.isDitto(secondPoke)) return null;
            parent = secondPoke;
        } else {
            if(parent.getGender().equals(Gender.MALE)) parent = secondPoke;
        }

        boolean isHA = isHA(parent);
        float chance = isHA ? 0.6f : 0.8f;
        float random = rand.nextFloat(0, 1.0F);

        Ability ability = null;

        if(isHA) {
            for (PotentialAbility speciesAbility : species.getAbilities()) {
                if(speciesAbility.getType().equals(HiddenAbilityType.INSTANCE)) ability = speciesAbility.getTemplate().create(false, Priority.NORMAL);
            }
        } else {
            if(parent.getSpecies().equals(species)) {
                ability = parent.getAbility();
            } else {
                List<PotentialAbility> potAbil = new ArrayList<>();
                species.getAbilities().forEach(abil -> {
                    if(abil.getType().equals(HiddenAbilityType.INSTANCE)) {
                        potAbil.add(abil);
                    }
                });

                PotentialAbility potentialAbility = potAbil.getFirst();
                if(potAbil.size() > 1) {
                    potentialAbility = potAbil.get(ModUtils.temptFate(0.5f, 0f, 1.0f) ? 0 : 1);
                }
                ability = potentialAbility.getTemplate().create(false, Priority.NORMAL);
            }
        }

        return (chance > random) ? ability : null;
    }

    private static List<MoveTemplate> getEggMoves(Species species, Pokemon poke1, Pokemon poke2) {
        List<MoveTemplate> learnset = getEggMoves(species);
        List<MoveTemplate> learned = new ArrayList<>();

        if(CobblemonUtils.isDitto(poke1) || CobblemonUtils.isDitto(poke2)) return learned;

        boolean differentSpecies = poke1.getSpecies() != poke2.getSpecies();

        if(poke1.getGender().equals(Gender.FEMALE)) {
            for(Move move : poke1.getMoveSet()) {
                if(learnset.contains(move.getTemplate())) learned.add(move.getTemplate());
            }
            if(poke2.getGender().equals(Gender.MALE) && differentSpecies) {
                for (Move move : poke2.getMoveSet()) {
                    if(learnset.contains(move.getTemplate())) learned.add(move.getTemplate());
                }
            }
        } else if(poke2.getGender().equals(Gender.FEMALE)){
            for(Move move : poke2.getMoveSet()) {
                if(learnset.contains(move.getTemplate())) learned.add(move.getTemplate());
            }
            if(poke1.getGender().equals(Gender.MALE) && differentSpecies) {
                for (Move move : poke1.getMoveSet()) {
                    if(learnset.contains(move.getTemplate())) learned.add(move.getTemplate());
                }
            }
        }

        return learned;
    }

    private static List<MoveTemplate> getEggMoves(Species species) {
        return species.getMoves().getEggMoves();
    }

    private static Species getBabyPokemon(Species species) {
        if(species.getPreEvolution() != null) {
            return getBabyPokemon(species.getPreEvolution().getSpecies());
        }
        return species;
    }

    public static boolean isHA(Pokemon pokemon) {
        for (PotentialAbility spAb : pokemon.getForm().getAbilities()) {
            if(pokemon.getAbility().getTemplate().getName().equals(spAb.getTemplate().getName())
                    && spAb.getType().equals(HiddenAbilityType.INSTANCE)) return true;
        }
        return false;
    }

}
