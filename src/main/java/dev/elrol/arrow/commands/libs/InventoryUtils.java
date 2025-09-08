package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.commands.ArrowCommands;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class InventoryUtils {

    @Nullable
    public static Inventory getInventoryAt(final @Nonnull World world, final @Nonnull BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if(block instanceof InventoryProvider inventory) {
            return inventory.getInventory(state, world, pos);
        } else {
            if (state.hasBlockEntity()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof Inventory inventory) {
                    if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                        return ChestBlock.getInventory((ChestBlock)block, state, world, pos, true);
                    } else {
                        List<Entity> list = world.getOtherEntities((Entity)null, new Box(pos.getX() - (double)0.5F, pos.getY() - (double)0.5F, pos.getZ() - (double)0.5F, pos.getX() + (double)0.5F, pos.getY() + (double)0.5F, pos.getZ() + (double)0.5F), EntityPredicates.VALID_INVENTORIES);
                        return !list.isEmpty() ? (Inventory)list.get(world.random.nextInt(list.size())) : null;
                    }
                }
            }
        }
        return null;
    }

    public static void addItems(Inventory inventory, ItemStack stack, int amount) {
        int total = stack.getCount() * amount;
        final int max = stack.getMaxCount();

        for(int i = 0; i < inventory.size(); i++) {
            ItemStack slot = inventory.getStack(i);
            if(ItemStack.areItemsEqual(slot, stack)) {
                int current = slot.getCount();
                int slotSpace = max - current;
                if(slotSpace <= 0) continue;
                if(slotSpace >= total) {
                    slot.increment(total);
                    inventory.markDirty();
                    return;
                } else {
                    slot.increment(slotSpace);
                    total -= slotSpace;
                }
            } else if(slot.isEmpty()) {
                int toTake = Math.min(max, total);
                inventory.setStack(i, stack.copyWithCount(toTake));
                total -= toTake;
                if(total <= 0) return;
            }
        }
        inventory.markDirty();
        ArrowCommands.LOGGER.error("Tried to add {} items from a player's inventory that didn't have enough of them", total);
    }

    public static void removeItems(Inventory inventory, ItemStack stack, int amount) {
        int total = stack.getCount() * amount;

        for(int i = 0; i < inventory.size(); i++) {
            ItemStack slot = inventory.getStack(i);
            if(ItemStack.areItemsEqual(slot, stack)) {
                int current = slot.getCount();
                if(current >= total) {
                    slot.decrement(total);
                    inventory.markDirty();
                    return;
                } else {
                    slot.decrement(current);
                    total -= current;
                }
            }
        }
        inventory.markDirty();
        ArrowCommands.LOGGER.error("Tried to remove {} items from a chest inventory that didn't have enough of them", total);
    }

    public static int getItemCount(Inventory inventory, ItemStack stack) {
        int count = 0;
        for(int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if(ItemStack.areItemsEqual(itemStack, stack)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }

    public static int getItemCountAt(World world, BlockPos pos, ItemStack stack) {
        Inventory inventory = getInventoryAt(world, pos);
        if(inventory == null) return 0;
        return getItemCount(inventory, stack);
    }

    public static int spaceLeft(Inventory inventory, ItemStack stack) {
        int count = 0;
        for(int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if(ItemStack.areItemsEqual(itemStack, stack)) {
                count += itemStack.getMaxCount() - itemStack.getCount();
            } else if(itemStack.isEmpty()) {
                count += stack.getMaxCount();
            }
        }
        return count;
    }
}
