package dev.elrol.arrow.commands.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.luckperms.api.util.Tristate;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KitData {

    public static final Codec<KitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.fieldOf("name").forGetter(data -> data.name),
            Codec.STRING.fieldOf("id").forGetter(data -> data.id),
            ItemStack.CODEC.listOf().optionalFieldOf("items").forGetter(data -> Optional.ofNullable(data.items)),
            Codec.STRING.listOf().optionalFieldOf("commands").forGetter(data -> Optional.ofNullable(data.commands)),
            Codec.BOOL.fieldOf("oneTimeUse").forGetter(data -> data.oneTimeUse),
            Codec.INT.fieldOf("cooldown").forGetter(data -> data.cooldown)
    ).apply(instance, (name, id, items, commands, oneTimeUse, cooldown) -> {
        KitData data = new KitData();

        data.name = name;
        data.id = id;
        items.ifPresent(data.items::addAll);
        commands.ifPresent(data.commands::addAll);
        data.oneTimeUse = oneTimeUse;
        data.cooldown = cooldown;

        return data;
    }));

    public Text name;
    public String id;
    public final List<ItemStack> items = new ArrayList<>();
    public final List<String> commands = new ArrayList<>();
    public boolean oneTimeUse = false;
    public int cooldown;

    public void giveKit(ServerPlayerEntity player) {
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        ArrowCommands.LOGGER.warn(result.getOrThrow().toString());
        for(ItemStack stack : items) {
            if(!player.giveItemStack(stack.copy())) {
                ItemEntity entity = new ItemEntity(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), stack.copy());
                player.getServerWorld().spawnEntity(entity);
            }
        }
        runCommands(player);
        player.sendMessage(
                ModTranslations.msg("kit_claim_1")
                        .append(name)
                        .append(ModTranslations.msg("kit_claim_2"))
        );
    }

    // Replace {player} with player's username
    // Replace {x} with player's X position
    // Replace {y} with player's Y position
    // Replace {z} with player's Z position

    private void runCommands(ServerPlayerEntity player) {
        MinecraftServer server = player.server;
        CommandManager manager = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        Vec3d pos = player.getPos();

        commands.forEach(command -> manager.executeWithPrefix(source, command
                .replace("{player}", player.getName().getString())
                .replace("{x}", String.valueOf(pos.x))
                .replace("{y}", String.valueOf(pos.x))
                .replace("{z}", String.valueOf(pos.x))));
    }

    public boolean hasPermission(ServerPlayerEntity player) {
        return PermUtils.hasPerm(player, "arrow.kit", id).equals(Tristate.TRUE);
    }

}
