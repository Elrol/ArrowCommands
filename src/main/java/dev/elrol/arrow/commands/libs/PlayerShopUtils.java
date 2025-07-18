package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.data.PlayerShopData;
import dev.elrol.arrow.commands.data.ShopData;
import dev.elrol.arrow.commands.data.TempShopData;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.data.PlayerData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerShopUtils {

    public static boolean isShop(World world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof IDisplayShop shop) {
            return shop.arrowcommands$isShop();
        }
        return false;
    }

    @Nullable
    public static ShopData getShop(UUID uuid, BlockPos pos) {
        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(uuid);
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        return commandData.playerShopData.getShop(pos);
    }

    public static boolean createShop(ServerPlayerEntity player) {
        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        PlayerShopData playerShopData = commandData.playerShopData;
        TempShopData tempShopData = playerShopData.tempShop;
        if(tempShopData == null) return false;

        BlockPos pos = tempShopData.shop.getDisplayCase();
        if(pos == null || isShop(player.getServerWorld(), pos)) return false;

        ShopData shop = tempShopData.shop;
        playerShopData.addShop(pos, shop);
        playerShopData.tempShop = null;
        commandData.playerShopData = playerShopData;
        data.put(commandData);

        return true;
    }

    public static void removeShop(PlayerEntity player, BlockPos pos) {
        if(!isShop(player.getWorld(), pos)) return;

        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        commandData.playerShopData.removeShop(pos);
        data.put(commandData, true);
    }
}
