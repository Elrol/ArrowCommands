package dev.elrol.arrow.commands.data;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.data.IPlayerData;
import dev.elrol.arrow.codecs.ArrowCodecs;
import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerDataCommands implements IPlayerData {

    public static final Codec<PlayerDataCommands> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DaycareData.CODEC.fieldOf("daycareData").forGetter(data -> data.daycareData),
                Codec.unboundedMap(Codec.STRING, ExactLocation.CODEC).fieldOf("homes").forGetter(data -> data.homes),
                ShoppingData.CODEC.fieldOf("shoppingData").forGetter(data -> data.shoppingData),
                OnTimeData.CODEC.optionalFieldOf("onTimeData").forGetter(data -> Optional.ofNullable(data.onTimeData)),
                Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("kitCooldownMap").forGetter(data -> Optional.empty()),
                Codec.unboundedMap(Codec.STRING, ArrowCodecs.DATE_TIME_CODEC).optionalFieldOf("kitTimeStamps").forGetter(data -> {
                    Map<String, LocalDateTime> map = data.kitTimeStamps;
                    return Optional.ofNullable(map);
                }),
                PlayerShopData.CODEC.optionalFieldOf("playerShopData").forGetter(data -> Optional.ofNullable(data.playerShopData))
        ).apply(instance, (daycareData, homes, shoppingData, onTimeData, kitCooldownMap,kitTimeStamps,playerShopData) -> {
            PlayerDataCommands data = new PlayerDataCommands();
            data.daycareData = daycareData;
            data.homes = new HashMap<>(homes);
            data.shoppingData = shoppingData;
            onTimeData.ifPresent(a -> data.onTimeData = a);
            kitTimeStamps.ifPresent(map -> data.kitTimeStamps.putAll(map));
            playerShopData.ifPresent(a -> data.playerShopData = a);
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
        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
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
        return "commands";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPlayerData> Codec<T> getCodec() {
        return (Codec<T>) CODEC;
    }
}
