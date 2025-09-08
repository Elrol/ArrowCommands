package dev.elrol.arrow.commands.data;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.data.IPlayerData;
import dev.elrol.arrow.codecs.ArrowCodecs;
import dev.elrol.arrow.data.ArrowPlayerData;
import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.data.PlayerDataType;
import dev.elrol.arrow.registries.PlayerDataTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataCommands implements IPlayerData {

    public static final Codec<PlayerDataCommands> CODEC;
    public static final MapCodec<PlayerDataCommands> MAP_CODEC;
    public static final String DATA_ID = "commands";

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DaycareData.CODEC.optionalFieldOf("daycareData", new DaycareData()).forGetter(data -> data.daycareData),
                Codec.unboundedMap(Codec.STRING, ExactLocation.CODEC).optionalFieldOf("homes", Map.of()).forGetter(data -> data.homes),
                ShoppingData.CODEC.optionalFieldOf("shoppingData", new ShoppingData()).forGetter(data -> data.shoppingData),
                OnTimeData.CODEC.optionalFieldOf("onTimeData", new OnTimeData()).forGetter(data -> data.onTimeData),
                Codec.unboundedMap(Codec.STRING, ArrowCodecs.DATE_TIME_CODEC).optionalFieldOf("kitTimeStamps", Map.of()).forGetter(data -> data.kitTimeStamps),
                PlayerShopData.CODEC.optionalFieldOf("playerShopData", new PlayerShopData()).forGetter(data -> data.playerShopData)
        ).apply(instance, (daycareData, homes, shoppingData, onTimeData, kitTimeStamps,playerShopData) -> {
            PlayerDataCommands data = new PlayerDataCommands();
            data.daycareData = daycareData;
            data.homes = new HashMap<>(homes);
            data.shoppingData = shoppingData;
            data.onTimeData = onTimeData;
            data.kitTimeStamps.putAll(kitTimeStamps);
            data.playerShopData = playerShopData;
            return data;
        }));

        MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DaycareData.CODEC.fieldOf("daycareData").forGetter(data -> data.daycareData),
                Codec.unboundedMap(Codec.STRING, ExactLocation.CODEC).fieldOf("homes").forGetter(data -> data.homes),
                ShoppingData.CODEC.fieldOf("shoppingData").forGetter(data -> data.shoppingData),
                OnTimeData.CODEC.optionalFieldOf("onTimeData", new OnTimeData()).forGetter(data -> data.onTimeData),
                Codec.unboundedMap(Codec.STRING, ArrowCodecs.DATE_TIME_CODEC).optionalFieldOf("kitTimeStamps", Map.of()).forGetter(data -> data.kitTimeStamps),
                PlayerShopData.CODEC.optionalFieldOf("playerShopData", new PlayerShopData()).forGetter(data -> data.playerShopData)
        ).apply(instance, (daycareData, homes, shoppingData, onTimeData, kitTimeStamps, playerShopData) -> {
            PlayerDataCommands data = new PlayerDataCommands();
            data.daycareData = daycareData;
            data.homes = new HashMap<>(homes);
            data.shoppingData = shoppingData;
            data.onTimeData = onTimeData;
            data.kitTimeStamps.putAll(kitTimeStamps);
            data.playerShopData = playerShopData;
            return data;
        }));
    }

    public DaycareData daycareData = new DaycareData();
    public ShoppingData shoppingData = new ShoppingData();
    public Map<String, ExactLocation> homes = new HashMap<>();
    public OnTimeData onTimeData = new OnTimeData();
    public Map<String, LocalDateTime> kitTimeStamps = new HashMap<>();
    public PlayerShopData playerShopData = new PlayerShopData();

    public boolean goHome(String home, ServerPlayerEntity player) {
        if(!homes.containsKey(home)) {
            return false;
        }
        homes.get(home).teleport(player);

        return true;
    }

    public void setHome(String home, ExactLocation pos) {
        homes.put(home, pos);
    }

    public void setHome(String home, ServerPlayerEntity player) {
        setHome(home, ExactLocation.from(player));
    }

    public boolean delHome(String home) {
        if(homes.containsKey(home)) {
            homes.remove(home);
            return true;
        }
        return false;
    }

    public boolean goBack(ServerPlayerEntity player){
        ArrowPlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
        PlayerDataCore coreData = data.get(new PlayerDataCore());
        if(coreData.teleportHistory.isEmpty()) return false;

        coreData.teleportHistory.getFirst().teleport(player, false);
        coreData.teleportHistory.removeFirst();
        data.put(coreData);
        return true;
    }

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
    }

    @Override
    public String getDataID() {
        return DATA_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPlayerData> Codec<T> getCodec() {
        return (Codec<T>) CODEC;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPlayerData> MapCodec<T> getMapCodec() {
        return (MapCodec<T>) MAP_CODEC;
    }

    @Override
    public PlayerDataType<?> getType() {
        return PlayerDataTypes.get(getDataID());
    }
}
