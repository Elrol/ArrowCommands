package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.interfaces.ILockable;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

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
