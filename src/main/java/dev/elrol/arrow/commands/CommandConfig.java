package dev.elrol.arrow.commands;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.config._BaseConfig;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;

public class CommandConfig extends _BaseConfig {

    public static final Codec<CommandConfig> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("rtpMin").forGetter(data -> data.rtpMin),
                Codec.INT.fieldOf("rtpMax").forGetter(data -> data.rtpMax),
                DaycareSettings.CODEC.fieldOf("daycareSettings").forGetter(data -> data.daycareSettings),
                SilkTouchSettings.CODEC.fieldOf("silkTouchSettings").forGetter(data -> data.silkTouchSettings),
                CustomItemSettings.CODEC.fieldOf("customItemSettings").forGetter(data -> data.customItemSettings),
                EconomySettings.CODEC.fieldOf("economySettings").forGetter(data -> data.economySettings),
                Codec.STRING.fieldOf("discordLink").forGetter(data -> data.discordLink)
        ).apply(instance, (rtpMin, rtpMax, daycareSettings, silkTouchSettings, customItemSettings, economySettings, discordLink) -> {
            CommandConfig data = new CommandConfig();
            data.rtpMin = rtpMin;
            data.rtpMax = rtpMax;
            data.daycareSettings = daycareSettings;
            data.silkTouchSettings = silkTouchSettings;
            data.customItemSettings = customItemSettings;
            data.economySettings = economySettings;
            data.discordLink = discordLink;
            return data;
        }));
    }

    public int rtpMin = 100;
    public int rtpMax = 5000;

    public DaycareSettings daycareSettings = new DaycareSettings();
    public SilkTouchSettings silkTouchSettings = new SilkTouchSettings();
    public CustomItemSettings customItemSettings = new CustomItemSettings();
    public EconomySettings economySettings = new EconomySettings();
    public String discordLink = "missing";

    public CommandConfig() {
        super("commands");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends _BaseConfig> Codec<T> getCodec() {
        return (Codec<T>) CODEC;
    }

    public static class EconomySettings {
        public static final Codec<EconomySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("startingBalance").forGetter(data -> data.startingBalance),
                Codec.STRING.listOf().fieldOf("info").forGetter(data -> Arrays.stream(data.info).toList()),
                Codec.STRING.fieldOf("expFormula").forGetter(data -> data.expFormula)
        ).apply(instance, (startingBalance, info, expFormula) -> {
            EconomySettings data = new EconomySettings();

            data.startingBalance = startingBalance;
            data.expFormula = expFormula;

            return data;
        }));

        public float startingBalance = 100.0f;

        private final String[] info = new String[] {
                "Valid Variables:",
                "   IVS to represent total IVs on the pokemon",
                "   LEVEL to represent the pokemon's level",
                "   SHINY to represent shiny status. 1 if shiny, 0 if not",
                "   LEGENDARY to represent legendary status. 1 if legendary, 0 if not",
                "   COIN to represent if a pokemon had the coin in battle. 1 if they did, 0 if not",
                "   PAYDAY to represent if a pokemon had used the move payday in battle. 1 if they did, 0 if not",
                "Example: \"(LEVEL + IVS + (PAYDAY * (LEVEL * 0.5))) * (SHINY + LEGENDARY + COIN + 1)\""
        };

        // (100 + (16) * (1) * (1 ^ 2)) 116
        public String expFormula = "(LEVEL + IVS + (PAYDAY * (LEVEL * 0.5))) * (SHINY + LEGENDARY + COIN + 1)";

        public int calcMoneyFromPokemon(Pokemon pokemon, boolean didUsePayDay, boolean isHoldingAmuletCoin) {
            Expression expression = new ExpressionBuilder(expFormula)
                    .variables("IVS", "LEVEL", "SHINY", "LEGENDARY", "COIN", "PAYDAY").build();

            Integer hpIV = pokemon.getIvs().get(Stats.HP);
            Integer atkIV = pokemon.getIvs().get(Stats.ATTACK);
            Integer defIV = pokemon.getIvs().get(Stats.DEFENCE);
            Integer spatkIV = pokemon.getIvs().get(Stats.SPECIAL_ATTACK);
            Integer spdefIV = pokemon.getIvs().get(Stats.SPECIAL_DEFENCE);
            Integer spdIV = pokemon.getIvs().get(Stats.SPEED);

            if(hpIV == null || atkIV == null || defIV == null || spatkIV == null || spdefIV == null || spdIV == null) {
                hpIV = 0;
                atkIV = 0;
                defIV = 0;
                spatkIV = 0;
                spdefIV = 0;
                spdIV = 0;
            }

            if(ArrowCore.CONFIG.isDebug) {
                ArrowCommands.LOGGER.warn("Did use Pay Day: {}", didUsePayDay);
                ArrowCommands.LOGGER.warn("Was Holding Amulet Coin: {}", isHoldingAmuletCoin);
            }

            expression.setVariable("IVS", (hpIV + atkIV + defIV + spatkIV + spdefIV + spdIV));
            expression.setVariable("LEVEL", pokemon.getLevel());
            expression.setVariable("SHINY", pokemon.getShiny() ? 1 : 0);
            expression.setVariable("LEGENDARY", pokemon.isLegendary() ? 1 : 0);
            expression.setVariable("COIN", isHoldingAmuletCoin ? 1 : 0);
            expression.setVariable("PAYDAY", didUsePayDay ? 1 : 0);

            return (int) expression.evaluate();
        }
    }

    public static class SilkTouchSettings {
        public static final Codec<SilkTouchSettings> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("enabledBlocks").forGetter(data -> data.enabledBlocks),
                    Codec.INT.fieldOf("pickDamagePerBlock").forGetter(data -> data.pickDamagePerBlock)
            ).apply(instance, (enabledBlocks, pickDamagePerBlock)->{
                SilkTouchSettings data = new SilkTouchSettings();

                data.enabledBlocks.clear();
                data.enabledBlocks.putAll(enabledBlocks);
                data.pickDamagePerBlock = pickDamagePerBlock;

                return data;
            }));
        }
        public final Map<String, Boolean> enabledBlocks = new HashMap<>();
        public int pickDamagePerBlock = 32;

        public Boolean get(String name) {
            if(enabledBlocks.containsKey(name)) return enabledBlocks.get(name);

            enabledBlocks.put(name, false);
            ArrowCommands.CONFIG.save();
            return false;
        }
    }

    public static class DaycareSettings {
        public static final Codec<DaycareSettings> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("doubleDittoBreeding").forGetter(data -> data.doubleDittoBreeding),
                    Codec.BOOL.fieldOf("dittoBreedingLegendaries").forGetter(data -> data.dittoBreedingLegendaries),
                    Codec.FLOAT.fieldOf("minutesToHatchEgg").forGetter(data -> data.minutesToHatchEgg)
            ).apply(instance, (doubleDittoBreeding, dittoBreedingLegendaries, minutesToHatchEgg) -> {
                DaycareSettings data = new DaycareSettings();
                data.doubleDittoBreeding = doubleDittoBreeding;
                data.dittoBreedingLegendaries = dittoBreedingLegendaries;
                data.minutesToHatchEgg = minutesToHatchEgg;
                return data;
            }));
        }

        public boolean doubleDittoBreeding = false;
        public boolean dittoBreedingLegendaries = false;
        public float minutesToHatchEgg = 1.0f;
    }

    public static class ShopItem {

    }

    public static class CustomItemSettings {
        public static final Codec<CustomItemSettings> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    AncientDNASettings.CODEC.fieldOf("ancientDNA").forGetter(data -> data.ancientDNA)
            ).apply(instance, (ancientDNA) -> {
                CustomItemSettings data = new CustomItemSettings();
                data.ancientDNA = ancientDNA;
                return data;
            }));
        }

        public AncientDNASettings ancientDNA = new AncientDNASettings();

    }

    public static class AncientDNASettings {
        public static final Codec<AncientDNASettings> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("dropChance").forGetter(data -> data.dropChance),
                    Codec.FLOAT.fieldOf("cloneDittoChance").forGetter(data -> data.cloneDittoChance),
                    Codec.FLOAT.fieldOf("cloneMewTwoChance").forGetter(data -> data.cloneMewTwoChance)
            ).apply(instance, (dropChance, dittoChance, mewTwoChance) -> {
                AncientDNASettings data = new AncientDNASettings();

                data.dropChance = dropChance;
                data.cloneDittoChance = dittoChance;
                data.cloneMewTwoChance = mewTwoChance;

                return data;
            }));
        }

        public float dropChance = 0.5f;
        public float cloneDittoChance = 75.0f;
        public float cloneMewTwoChance = 20.0f;
    }


}
