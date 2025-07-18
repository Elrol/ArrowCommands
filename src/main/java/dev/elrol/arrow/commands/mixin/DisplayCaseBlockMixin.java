package dev.elrol.arrow.commands.mixin;

import com.cobblemon.mod.common.block.DisplayCaseBlock;
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.ShopData;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.libs.PlayerShopUtils;
import dev.elrol.arrow.commands.menus.shops.ItemSelectMenu;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(DisplayCaseBlock.class)
public class DisplayCaseBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void arrowcommands$preventUpdate(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir) {
        IDisplayShop displayShop = (IDisplayShop) world.getBlockEntity(pos);
        ArrowCommands.LOGGER.error("Test");
        assert displayShop != null;
        if(displayShop.arrowcommands$isShop() && player instanceof ServerPlayerEntity serverPlayer) {
            UUID shopOwner = displayShop.arrowcommands$getOwner();
            if (Objects.equals(shopOwner, player.getUuid())) {
                player.sendMessage(Text.literal("You used your own shop"));
            } else {
                // ToDo open menu here to buy from/sell to shop
                ShopData shopData = PlayerShopUtils.getShop(shopOwner, pos);
                if(shopData != null) {
                    ItemSelectMenu itemSelectMenu = (ItemSelectMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("item_select", serverPlayer);

                    itemSelectMenu.setConfirmFunction(listing -> {

                    });

                    itemSelectMenu.setCancelFunction(() -> {

                    });

                    itemSelectMenu.open(shopData.getListing(serverPlayer.getServerWorld()), true);

                    player.sendMessage(Text.literal("You used a shop"));
                }
            }
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
