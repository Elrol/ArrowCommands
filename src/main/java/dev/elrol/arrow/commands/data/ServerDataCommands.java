package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.data.IServerData;
import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerDataCommands implements IServerData {

    public static final Codec<ServerDataCommands> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExactLocation.CODEC.fieldOf("spawnLocation").forGetter(data -> data.spawnLocation),
                Codec.unboundedMap(Codec.STRING, ExactLocation.CODEC).fieldOf("warpLocations").forGetter(data -> data.warpLocations)
        ).apply(instance, (spawnLocation, warpLocations) -> {
            ServerDataCommands data = new ServerDataCommands();
            data.spawnLocation = spawnLocation;
            data.warpLocations.putAll(warpLocations);
            return data;
        }));
    }

    private ExactLocation spawnLocation;
    private final Map<String, ExactLocation> warpLocations = new HashMap<>();


    public void setSpawnLocation(ServerPlayerEntity player) {
        setSpawnLocation(ExactLocation.from(player));
    }

    public void setSpawnLocation(ServerWorld world, double x, double y, double z) {
        setSpawnLocation(world, x,y,z,0f,0f);
    }

    public void setSpawnLocation(ServerWorld world, double x, double y, double z, float yaw, float pitch) {
        setSpawnLocation(new ExactLocation(world,x,y,z,yaw,pitch));
    }

    public void setSpawnLocation(ExactLocation location) {
        spawnLocation = location;
        ArrowCore.INSTANCE.getServerDataRegistry().save();
    }

    public ExactLocation getSpawnLocation() {
        return spawnLocation;
    }

    public void addWarp(ServerPlayerEntity player, String name) {
        name = name.toLowerCase();
        if(warpLocations.containsKey(name)) {
            player.sendMessage(ModTranslations.msg("warp_set_1")
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(ModTranslations.msg("warp_set_2"))
            );
        } else {
            player.sendMessage(ModTranslations.msg("warp_created_1")
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(ModTranslations.msg("warp_created_2"))
            );
        }
        warpLocations.put(name, ExactLocation.from(player));
        ArrowCore.INSTANCE.getServerDataRegistry().save();
    }

    public boolean removeWarp(String name) {
        name = name.toLowerCase();
        if(warpLocations.containsKey(name)) {
            warpLocations.remove(name);
            ArrowCore.INSTANCE.getServerDataRegistry().save();
            return true;
        }
        else{
            return false;
        }
    }

    public Set<String> getWarpsNames() {
        return warpLocations.keySet();
    }

    public void teleportToSpawn(ServerPlayerEntity player) {
        if(spawnLocation == null){
            BlockPos pos = player.getServerWorld().getSpawnPos();
            player.teleport(player.getServerWorld(), pos.getX(), pos.getY(), pos.getZ(), 0f, 0f);
        } else {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCore coreData = data.get(new PlayerDataCore());
            coreData.logTeleport(player);
            data.put(coreData);

            spawnLocation.teleport(player);
        }
    }

    public boolean teleportToWarp(ServerPlayerEntity player, String name) {
        name = name.toLowerCase();
        if(warpLocations.containsKey(name)) {
            warpLocations.get(name).teleport(player);
            return true;
        }
        return false;
    }


    @Override
    public String getDataID() {
        return "commands";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IServerData> Codec<T> getCodec() {
        return (Codec<T>) CODEC;
    }


}
