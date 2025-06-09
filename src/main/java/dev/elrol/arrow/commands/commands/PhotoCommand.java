package dev.elrol.arrow.commands.commands;

import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.CobblemonUtils;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PhotoCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(CommandManager.literal("photo")
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "photo").asBoolean();
                    }
                    return true;
                })
                .then(argument("slot", IntegerArgumentType.integer(1,6))
                        .executes(this::oneArgs)
                ));
    }

    private int oneArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        int slot = IntegerArgumentType.getInteger(context, "slot");

        if(player != null) {
            Pokemon pokemon = CobblemonUtils.getSlot(player, slot);
            if(pokemon != null) {
                ItemStack photo = PokemonItem.from(pokemon);
                player.giveItemStack(photo);
                player.sendMessage(ModTranslations.msg("photo_taken"));
            } else {
                player.sendMessage(ModTranslations.err("missing_pokemon", slot));
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}
