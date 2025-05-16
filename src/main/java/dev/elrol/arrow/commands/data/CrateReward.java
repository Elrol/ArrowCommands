package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;

public class CrateReward {

    public static final Codec<CrateReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("icon").forGetter(data -> data.icon),
            TextCodecs.CODEC.fieldOf("name").forGetter(data -> data.name),
            Codec.INT.fieldOf("weight").forGetter(data -> data.weight),
            Codec.STRING.listOf().fieldOf("cmdRewards").forGetter(data -> data.cmdRewards),
            ItemStack.CODEC.listOf().fieldOf("itemRewards").forGetter(data -> data.itemRewards)
    ).apply(instance, (icon, name, weight, cmdRewards, itemRewards) -> {
        CrateReward data = new CrateReward();
        data.icon = icon;
        data.name = name;
        data.weight = weight;
        data.cmdRewards.addAll(cmdRewards);
        data.itemRewards.addAll(itemRewards);
        return data;
    }));

    public ItemStack icon = ItemStack.EMPTY;
    public Text name = Text.empty();
    public int weight = 1;

    public List<String> cmdRewards = new ArrayList<>();
    public List<ItemStack> itemRewards = new ArrayList<>();

    public ItemStack getIcon() {
        ItemStack stack = icon.copy();
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
        return stack;
    }

    public void give(ServerPlayerEntity player) {
        final String username = player.getName().getString();
        final MinecraftServer server = player.server;
        final ServerCommandSource source = server.getCommandSource();

        cmdRewards.forEach(cmd -> server.getCommandManager().executeWithPrefix(source, cmd.replace("{player}", username)));
        itemRewards.forEach(item -> player.giveItemStack(item.copy()));
    }

}
