package dev.elrol.arrow.commands.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.CrateData;
import dev.elrol.arrow.commands.data.CrateReward;
import dev.elrol.arrow.libs.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrateRegistry {

    private static final File dir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "/Arrow/Crates");
    static Map<String, CrateData> crates = new HashMap<>();

    public static Map<UUID, CrateReward> tempRewardMap = new HashMap<>();

    public static void grantRewards(ServerPlayerEntity player) {
        if(tempRewardMap.containsKey(player.getUuid())) {
            tempRewardMap.get(player.getUuid()).give(player);
            tempRewardMap.remove(player.getUuid());
        }
    }

    public static void register(MinecraftServer server) {
        crates.clear();
        tempRewardMap.clear();

        if(!dir.mkdirs()) {
            load(server);
        }

        if(crates.isEmpty()) {
            ItemStack book = new ItemStack(Items.IRON_PICKAXE);

            RegistryEntry.Reference<Enchantment> entry = server.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.MENDING).get();
            book.addEnchantment(entry, 1);
            ServerWorld world = server.getOverworld();
            ItemEntity itemEntity = new ItemEntity(world, 0, 100, 0,book);
            world.spawnEntity(itemEntity);

            CrateData crate = new CrateData("example", new ItemStack(Items.SNOWBALL));
            CrateReward reward = new CrateReward();
            reward.icon = new ItemStack(Items.BEDROCK);
            reward.name = Text.literal("Example").formatted(Formatting.RED, Formatting.BOLD);
            reward.itemRewards.add(new ItemStack(Items.BEDROCK, 10));
            reward.itemRewards.add(book);
            reward.cmdRewards.add("say {player} is testing the example crate");
            crate.name = Text.literal("Example ").formatted(Formatting.GOLD).append(Text.literal("Crate").formatted(Formatting.GREEN));
            crate.crateKey = new ItemStack(Items.STICK);
            crate.addReward(reward);
            register(crate);
            save(server);
        }

        crates.remove("example");
    }

    public static void register(CrateData crate) {
        String id = crate.id;
        if(crates.containsKey(id)) {
            ArrowCommands.LOGGER.error("The crate {} is already in use!", id);
            return;
        }
        crate.calcTotalChance();
        crates.put(id, crate);
    }

    public static Set<String> getIDs() {
        return crates.keySet();
    }

    @Nullable
    public static CrateData get(String id) {
        return crates.get(id);
    }

    public static void load(MinecraftServer server) {
        File[] files = dir.listFiles((file) -> file.getName().endsWith(".json"));

        assert files != null;
        for (File file : files) {
            JsonElement json = JsonUtils.loadFromJson(dir, file.getName(), JsonParser.parseString("{}"));

            DataResult<Pair<CrateData, JsonElement>> result = CrateData.CODEC.decode(RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), json);
            register(result.getOrThrow().getFirst());
        }
    }

    public static void save(MinecraftServer server) {
        crates.forEach((id, crate) -> {
            DataResult<JsonElement> json = CrateData.CODEC.encodeStart(RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), crate);
            JsonUtils.saveToJson(dir, id + ".json", json.getOrThrow());
        });
    }

}
