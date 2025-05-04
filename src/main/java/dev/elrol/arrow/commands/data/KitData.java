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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;

public class KitData {

    public static final Codec<KitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.fieldOf("name").forGetter(data -> data.name),
            Codec.STRING.fieldOf("id").forGetter(data -> data.id),
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(data -> data.items),
            Codec.BOOL.fieldOf("oneTimeUse").forGetter(data -> data.oneTimeUse),
            Codec.INT.fieldOf("cooldown").forGetter(data -> data.cooldown)
    ).apply(instance, (name, id, items, oneTimeUse, cooldown) -> {
        KitData data = new KitData();

        data.name = name;
        data.id = id;
        data.items.addAll(items);
        data.oneTimeUse = oneTimeUse;
        data.cooldown = cooldown;

        return data;
    }));

    public Text name;
    public String id;
    public final List<ItemStack> items = new ArrayList<>();
    public boolean oneTimeUse = false;
    public int cooldown;

    public void giveKit(ServerPlayerEntity player) {
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        ArrowCommands.LOGGER.warn(result.getOrThrow().toString());
        for(ItemStack stack : items) {
            if(!player.giveItemStack(stack.copy())) {
                ItemEntity entity = new ItemEntity(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), stack.copy());
                //entity.addStatusEffect()
                player.getServerWorld().spawnEntity(entity);
            }
        }
        player.sendMessage(
                ModTranslations.msg("kit_claim_1")
                        .append(name)
                        .append(ModTranslations.msg("kit_claim_2"))
        );
    }

    public boolean hasPermission(ServerPlayerEntity player) {
        return PermUtils.hasPerm(player, "arrow.kit", id).equals(Tristate.TRUE);
    }

}
