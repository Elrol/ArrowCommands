package dev.elrol.arrow.commands.data;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.data.IPlayerData;
import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.data.PlayerDataCore;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bson.codecs.jsr310.LocalDateTimeCodec;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataCommands implements IPlayerData {

    public static final Codec<PlayerDataCommands> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DaycareData.CODEC.fieldOf("daycareData").forGetter(data -> data.daycareData),
            Codec.unboundedMap(Codec.STRING, ExactLocation.CODEC).fieldOf("homes").forGetter(data -> data.homes),
            ShoppingData.CODEC.fieldOf("shoppingData").forGetter(data -> data.shoppingData),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("kitCooldownMap").forGetter(data -> data.kitCooldownMap)
        ).apply(instance, (daycareData, homes, shoppingData, kitCooldownMap) -> {
            PlayerDataCommands data = new PlayerDataCommands();
            data.daycareData = daycareData;
            data.homes = new HashMap<>(homes);
            data.shoppingData = shoppingData;
            data.kitCooldownMap.putAll(kitCooldownMap);
            return data;
        }));
    }

    public DaycareData daycareData = new DaycareData();
    public ShoppingData shoppingData = new ShoppingData();
    public Map<String, ExactLocation> homes = new HashMap<>();
    public LocalDateTime lastOnline;
    public Map<String, Integer> kitCooldownMap = new HashMap<>();

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
        PlayerDataCore data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player).get(new PlayerDataCore());
        if(data.teleportHistory.isEmpty()) return false;

        data.teleportHistory.getFirst().teleport(player, false);
        data.teleportHistory.removeFirst();
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
