package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.data.ExactLocation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldPlayerData {

    public static final Codec<OldPlayerData> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, OldExactLocation.CODEC).fieldOf("homes").forGetter(data -> data.homes),
                OldExactLocation.CODEC.listOf().fieldOf("teleportHistory").forGetter(data -> data.teleportHistory)
        ).apply(instance, (homes, teleportHistory)-> {
            OldPlayerData data = new OldPlayerData();

            data.homes = new HashMap<>(homes);
            data.teleportHistory = new ArrayList<>(teleportHistory);
            return data;
        }));
    }

    public Map<String, OldExactLocation> homes = new HashMap<>();
    public List<OldExactLocation> teleportHistory = new ArrayList<>();

    public static class OldExactLocation {
        public static final Codec<OldExactLocation> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                OldBlockPos.CODEC.fieldOf("position").forGetter(data -> data.position),
                    Codec.FLOAT.fieldOf("yaw").forGetter(data -> data.yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(data -> data.pitch),
                    Codec.STRING.fieldOf("world").forGetter(data -> data.world.toString())
            ).apply(instance, (position, yaw, pitch, world)->{
                OldExactLocation data = new OldExactLocation();

                data.position = position;
                data.yaw = yaw;
                data.pitch = pitch;
                data.world = Identifier.of(world);

                return data;
            }));
        }

        OldBlockPos position;
        float yaw;
        float pitch;
        Identifier world;

        public ExactLocation toExact() {
            return new ExactLocation(world.toString(), position.toVector(), yaw, pitch);
        }
    }

    public static class OldBlockPos {
        public static final Codec<OldBlockPos> CODEC;

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("field_1352").forGetter(data -> data.field_1352),
                Codec.DOUBLE.fieldOf("field_1351").forGetter(data -> data.field_1351),
                Codec.DOUBLE.fieldOf("field_1350").forGetter(data -> data.field_1350)
            ).apply(instance, (x, y, z)->{
                OldBlockPos data = new OldBlockPos();

                data.field_1352 = x;
                data.field_1351 = y;
                data.field_1350 = z;

                return data;
            }));
        }

        public double field_1352;
        public double field_1351;
        public double field_1350;

        Vec3d toVector() {
            return new Vec3d(field_1352, field_1351, field_1350);
        }
    }

}
