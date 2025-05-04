package dev.elrol.arrow.commands.libs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnerUtils {

    public static void fromItemStack(MobSpawnerBlockEntity spawner, ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if(lore != null && !lore.lines().isEmpty()) spawner.getLogic().readNbt(spawner.getWorld(), spawner.getPos(), fromLore(lore.styledLines()));
    }

    public static ItemStack toItemStack(MobSpawnerBlockEntity spawner){
        ItemStack stack = new ItemStack(Blocks.SPAWNER, 1);
        NbtCompound nbt = spawner.getLogic().writeNbt(new NbtCompound());
        Entity entity = spawner.getLogic().getRenderedEntity(spawner.getWorld(), spawner.getPos());
        //TextUtils.stringFormat(entity.getType().getName().getString() + " Spawner", Formatting.GREEN)
        if(entity != null) stack.apply(DataComponentTypes.CUSTOM_NAME, Text.empty(), ModTranslations.translate("arrow.spawner.name", entity.getType().getName().getString()).formatted(Formatting.GREEN), ((text, mutableText) -> mutableText));

        List<Text> lines = toLore(nbt);
        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lines, (a,b) -> new LoreComponent(b));
        return stack;
    }

    public static NbtCompound fromLore(List<Text> lore) {
        NbtCompound nbt = new NbtCompound();
        ArrowCommands.LOGGER.warn("Lore Size: {}", lore.size());

        for(int i = 0; i < lore.size(); i++) {
            String string = lore.get(i).getString();
            if(string.contains(" : ")){
                String value = string.split(" : ")[1];
                if(i == 0) {
                    try {
                        NbtCompound spawnData = StringNbtReader.parse("{entity:{id:\"" + value + "\"}}");
                        nbt.put("SpawnData", spawnData);
                    } catch (CommandSyntaxException e) {
                        ArrowCommands.LOGGER.error(e.getLocalizedMessage());
                    }
                } else {
                    if(i == 2) nbt.putShort("delay", Short.parseShort(value));
                    nbt.putShort(getNBTKey(i), Short.parseShort(value));
                }
            }
        }
        if(ArrowCore.CONFIG.isDebug)
            ArrowCommands.LOGGER.warn("Lore NBT: {}", nbt);
        return nbt;
    }

    public static List<Text> toLore(NbtCompound nbt) {
        if(ArrowCore.CONFIG.isDebug)
            ArrowCommands.LOGGER.warn("Lore Tag: {}", nbt);

        short minSpawnDelay = nbt.getShort("MinSpawnDelay");
        short maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
        short spawnCount = nbt.getShort("SpawnCount");
        short maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
        short requiredPlayerRange = nbt.getShort("RequiredPlayerRange");
        short spawnRange = nbt.getShort("SpawnRange");

        List<Text> lore = new ArrayList<>();

        MobSpawnerEntry entry;
        if(nbt.contains("SpawnData")) {
            entry = MobSpawnerEntry.CODEC.decode(NbtOps.INSTANCE, nbt.get("SpawnData")).getOrThrow(string -> new IllegalStateException("Invalid SpawnData: " + string)).getFirst();
            if(entry == null) {
                lore.add(toLoreLine("mob", ModTranslations.translate("arrow.spawner.lore.mob.empty")));
            } else {
                Optional<EntityType<?>> optional = EntityType.fromNbt(entry.getNbt());
                optional.ifPresentOrElse(entityType -> lore.add(toLoreLine("mob", ModTranslations.literal(Registries.ENTITY_TYPE.getId(entityType).toString()))), () -> lore.add(toLoreLine("mob", ModTranslations.translate("arrow.spawner.lore.mob.empty"))));
            }
        } else {
            lore.add(toLoreLine("mob", ModTranslations.translate("arrow.spawner.lore.mob.empty")));
        }

        lore.add(toLoreLine("min_spawn_delay",  ModTranslations.literal(minSpawnDelay + "")));
        lore.add(toLoreLine("max_spawn_delay",  ModTranslations.literal(maxSpawnDelay + "")));
        lore.add(toLoreLine("spawn_count",      ModTranslations.literal(spawnCount + "")));
        lore.add(toLoreLine("max_entities",     ModTranslations.literal(maxNearbyEntities + "")));
        lore.add(toLoreLine("player_range",     ModTranslations.literal(requiredPlayerRange + "")));
        lore.add(toLoreLine("spawn_range",      ModTranslations.literal(spawnRange + "")));

        return lore;
    }

    private static Text toLoreLine(String name, MutableText value) {
        return ModTranslations.translate("arrow.spawner.lore." + name).formatted(Formatting.GOLD)
                .append(ModTranslations.literal(" : ").formatted(Formatting.DARK_GRAY))
                .append(value.formatted(Formatting.GRAY));
    }

    private static String getNBTKey(int index) {
        return switch (index){
            case 0 -> "SpawnData";
            case 1 -> "MinSpawnDelay";
            case 2 -> "MaxSpawnDelay";
            case 3 -> "SpawnCount";
            case 4 -> "MaxNearbyEntities";
            case 5 -> "RequiredPlayerRange";
            case 6 -> "SpawnRange";
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }
}
