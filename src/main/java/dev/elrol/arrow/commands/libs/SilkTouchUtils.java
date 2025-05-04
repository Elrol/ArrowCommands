package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SilkTouchUtils {

    public static boolean attemptSilkTouch(PlayerEntity player, BlockState state, BlockPos pos, Block targetBlock) {
        TagKey<Item> PICKAXES = TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "pickaxes"));
        World world = player.getWorld();
        MutableText blockName = targetBlock.getName();

        ItemStack stack = player.getMainHandStack();
        if(!stack.isEmpty() && stack.isIn(PICKAXES)) {
            if(EnchantmentHelper.getLevel(player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), stack) >= 1) {
                ItemStack item = new ItemStack(state.getBlock().asItem());
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if(targetBlock.equals(Blocks.SPAWNER) && blockEntity instanceof MobSpawnerBlockEntity spawnerEntity) {
                    item = SpawnerUtils.toItemStack(spawnerEntity);
                }

                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), item));
                player.sendMessage(blockName.append(ModTranslations.translate("arrow.silk.dropped").formatted(Formatting.GREEN)));
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                stack.damage(ArrowCommands.CONFIG.silkTouchSettings.pickDamagePerBlock, player, EquipmentSlot.MAINHAND);
                return false;
            } else {
                if(!player.isSneaking()) {
                    player.sendMessage(ModTranslations.translate("arrow.silk.can_silk_1").formatted(Formatting.YELLOW)
                            .append(blockName)
                            .append(ModTranslations.translate("arrow.silk.can_silk_2").formatted(Formatting.YELLOW)));
                    return false;
                } else {
                    player.sendMessage(blockName.append(ModTranslations.translate("arrow.silk.broken").formatted(Formatting.RED)));
                    return true;
                }
            }

        }
        return true;
    }

}
