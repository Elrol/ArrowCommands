package dev.elrol.arrow.commands.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.arrow.commands.data.KitData;
import dev.elrol.arrow.libs.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class KitRegistry {
    private static final File dir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "/Arrow/Kits");

    private static final Map<String, KitData> kitMap = new HashMap<>();

    public static boolean exists(String ID) {
        return kitMap.containsKey(ID);
    }

    @Nullable
    public static KitData get(String ID) {
        return kitMap.getOrDefault(ID, null);
    }

    public static Map<String, KitData> get() {
        return new HashMap<>(kitMap);
    }

    public static void save() {
        for(String key : kitMap.keySet()) save(key);
    }

    public static void save(String id) {
        KitData kit = kitMap.get(id);
        DataResult<JsonElement> json = KitData.CODEC.encodeStart(JsonOps.INSTANCE, kit);
        JsonUtils.saveToJson(dir, kit.id + ".json", json.getOrThrow());
    }

    public static void load(MinecraftServer server) {
        if(!dir.mkdirs()) {
            kitMap.clear();

            File[] files = dir.listFiles();

            if(files != null) {
                for(File file : files) {
                    String id = file.getName().replace(".json", "");
                    JsonElement json = JsonUtils.loadFromJson(dir, file.getName(), JsonParser.parseString("{}"));
                    DataResult<Pair<KitData, JsonElement>> dataPair = KitData.CODEC.decode(RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), json);
                    if(dataPair.isSuccess()) {
                        kitMap.put(id, dataPair.getOrThrow().getFirst());
                    }
                }
            }
        }
        if(kitMap.isEmpty()) {
            KitData kit = new KitData();
            kit.name = Text.literal("Example").formatted(Formatting.RED);
            kit.id = "example";
            kit.items.add(new ItemStack(Items.DIAMOND, 1));
            kitMap.put("example", kit);
            save();
        }
    }

}
