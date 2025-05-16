package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrateData {

    public static final Codec<CrateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(data -> data.id),
            TextCodecs.CODEC.fieldOf("name").forGetter(data -> data.name),
            ItemStack.CODEC.fieldOf("icon").forGetter(data -> data.icon),
            ItemStack.CODEC.fieldOf("crateKey").forGetter(data -> data.crateKey),
            CrateReward.CODEC.listOf().fieldOf("rewards").forGetter(data -> data.rewards)
    ).apply(instance, (id, name, icon, crateKey, rewards) -> {
        CrateData data = new CrateData(id, icon);
        data.name = name;
        data.crateKey = crateKey;
        data.rewards.addAll(rewards);
        return data;
    }));

    public String id;
    public Text name;
    public ItemStack icon;
    public ItemStack crateKey;
    public List<CrateReward> rewards = new ArrayList<>();

    private final Random rand = new Random();
    public int totalChance = 0;

    public void addRewards(List<CrateReward> rewardList) { rewards.addAll(rewardList); }
    public void addReward(CrateReward reward) { rewards.add(reward); }

    public CrateData(String id, ItemStack icon) {
        this.id = id;
        this.icon = icon;
    }

    @NotNull
    public CrateReward pull() {
        int selected = rand.nextInt(totalChance);
        for (CrateReward reward : rewards) {
            if(selected < reward.weight) return reward;
            selected -= reward.weight;
        }
        return rewards.getLast();
    }

    public void calcTotalChance() {
        totalChance = 0;

        rewards.forEach(reward -> totalChance += reward.weight);
    }
}
