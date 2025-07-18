package dev.elrol.arrow.commands.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ServerShopData;
import dev.elrol.arrow.libs.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerShopRegistry {

    private static final File dir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "/Arrow/Shops");
    static Map<String, ServerShopData> serverShops = new HashMap<>();

    public static Map<String, ServerShopData> getServerShops() { return serverShops; }

    @Nullable
    public static ServerShopData get(String id) { return serverShops.get(id); }

    public static void register(MinecraftServer server) {
        serverShops.clear();

        if(!dir.mkdirs()) {
            load(server);
        }

        if(serverShops.isEmpty()) {
            register(new ServerShopData());
            save(server);
        }

        serverShops.remove("example");
    }

    public static void register(ServerShopData shop) {
        String shopName = shop.shopID;
        if(serverShops.containsKey(shopName)) {
            ArrowCommands.LOGGER.error("The shop {} is already in use!", shopName);
            return;
        }
        serverShops.put(shopName, shop);
    }

    public static void load(MinecraftServer server) {
        File[] files = dir.listFiles((file) -> file.getName().endsWith(".json"));

        assert files != null;
        for (File file : files) {
            JsonElement json = JsonUtils.loadFromJson(dir, file.getName(), JsonParser.parseString("{}"));

            DataResult<Pair<ServerShopData, JsonElement>> result = ServerShopData.CODEC.decode(RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), json);
            register(result.getOrThrow().getFirst());
        }
    }

    public static void save(MinecraftServer server) {
        serverShops.forEach((shopId, shop) -> {
            DataResult<JsonElement> json = ServerShopData.CODEC.encodeStart(RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), shop);
            JsonUtils.saveToJson(dir, shopId + ".json", json.getOrThrow());
        });
    }

}
