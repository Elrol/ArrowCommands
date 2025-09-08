package dev.elrol.arrow.commands.libs;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands.data.*;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.registries.ShopSaleDataTypes;
import dev.elrol.arrow.data.ArrowPlayerData;
import dev.elrol.arrow.libs.CobblemonUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
        ArrowPlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(uuid);
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        return commandData.playerShopData.getShop(pos);
    }

    public static boolean createShop(ServerPlayerEntity player) {
        ArrowPlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        PlayerShopData playerShopData = commandData.playerShopData;
        TempShopData tempShopData = playerShopData.tempShop;
        if(tempShopData == null) return false;

        BlockPos pos = tempShopData.shop.getDisplayCase();
        if(pos == null || isShop(player.getServerWorld(), pos)) return false;

        ShopData shop = tempShopData.shop;

        if(tempShopData.getShopType().equals(ShopSaleDataTypes.POKEMON_SHOP)) {
            PokemonShopSaleData pokemonShopSaleData = ((PokemonShopSaleData) shop.saleData);
            PlayerPartyStore party = CobblemonUtils.getParty(player);
            Pokemon pokemon = party.get(pokemonShopSaleData.slot);
            party.remove(new PartyPosition(pokemonShopSaleData.slot));
            if(pokemon == null) {
                return false;
            }
            pokemonShopSaleData.setPokemon(pokemon);
            shop.saleData = pokemonShopSaleData;
        }

        playerShopData.addShop(pos, shop);
        playerShopData.tempShop = null;
        commandData.playerShopData = playerShopData;
        data.put(commandData, true);

        BlockEntity entity = player.getServerWorld().getBlockEntity(pos);

        if(entity instanceof DisplayCaseBlockEntity caseEntity) {
            IDisplayShop displayShop = BlockUtils.getDisplayShop(caseEntity);

            assert displayShop != null;
            displayShop.arrowcommands$setOwner(player.getUuid());
            displayShop.arrowcommands$lock();
            ArrowCommands.debug("Is locked: " + displayShop.arrowcommands$locked());

            caseEntity.setStack(0, shop.saleData.getDisplayItem());
        }

        return true;
    }

    public static void removeShop(ServerPlayerEntity player, BlockPos pos) {
        if(!isShop(player.getWorld(), pos)) {
            ArrowCommands.LOGGER.error("Tried to remove a shop that isn't a shop at {}", pos.toShortString());
            return;
        }

        ArrowPlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
        ShopData shop = commandData.playerShopData.getShop(pos);
        if(shop != null && shop.saleData instanceof PokemonShopSaleData pokeSaleData) {
            pokeSaleData.givePokemon(player);
        }
        commandData.playerShopData.removeShop(pos);
        data.put(commandData, true);
    }

    public static boolean isInOverworld(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if(server == null) return false;
        return server.getWorld(World.OVERWORLD) == player.getServerWorld();
    }
}
