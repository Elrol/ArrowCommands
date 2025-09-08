package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.interfaces.ILockable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockUtils {

    @Nullable
    public static IDisplayShop getDisplayShop(BlockEntity entity) {
        if(entity instanceof IDisplayShop shop) {
            return shop;
        }
        return null;
    }

    @Nullable
    public static ILockable getLockable(BlockEntity entity) {
        if(entity instanceof IDisplayShop shop) {
            return shop;
        }
        return null;
    }

}
