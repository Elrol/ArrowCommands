package dev.elrol.arrow.commands.mixin;

import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DisplayCaseBlockEntity.class)
public class DisplayCaseEntityMixin implements IDisplayShop {

    @Unique
    private boolean locked = false;

    @Unique
    private UUID owner;

    @Inject(method = "updateItem", at = @At("HEAD"), cancellable = true)
    public void arrowcommands$preventUpdate(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(arrowcommands$locked() && player instanceof ServerPlayerEntity serverPlayer) {
            DisplayCaseBlockEntity target = (DisplayCaseBlockEntity)(Object) this;
            target.markDirty();
            BlockState state = serverPlayer.getServerWorld().getBlockState(target.getPos());
            serverPlayer.getServerWorld().updateListeners(target.getPos(), state, state, 0);
            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(target.getPos(), state));
            serverPlayer.getInventory().updateItems();
            //serverPlayer.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, ));
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void arrowcommands$writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.putBoolean("arrowcommands$locked", locked);
        if(owner != null) nbt.putString("arrowcommands$owner", owner.toString());
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void arrowcommands$readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        locked = nbt.getBoolean("arrowcommands$locked");
        String uuid = nbt.getString("arrowcommands$owner");
        if(uuid != null) {
            arrowcommands$setOwner(UUID.fromString(uuid));
        }
    }

    @Override
    public void arrowcommands$lock() {
        locked = true;
    }

    @Override
    public void arrowcommands$unlock() {
        locked = false;
    }

    @Override
    public boolean arrowcommands$locked() {
        return locked;
    }

    @Override
    public UUID arrowcommands$getOwner() {
        return owner;
    }

    @Override
    public void arrowcommands$setOwner(UUID uuid) {
        owner = uuid;
    }

    @Override
    public boolean arrowcommands$isShop() {
        return owner != null;
    }
}
