package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.KitSuggestionProvider;
import dev.elrol.arrow.commands.data.KitData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.registries.KitRegistry;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class KitCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("kit").requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "kit").asBoolean();
                    }
                    return true;
                })
                .then(CommandManager.argument("kit", StringArgumentType.string())
                        .suggests(new KitSuggestionProvider(false))
                        .executes(this::oneArgs)));
    }

    private int oneArgs(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String ID = StringArgumentType.getString(context, "kit");
        if(player != null) {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            KitData kit = KitRegistry.get(ID);

            if(kit == null || !kit.hasPermission(player)) return 0;

            if(commandData.kitCooldownMap.containsKey(ID)) {
                player.sendMessage(ModTranslations.err(kit.oneTimeUse ? "kit_claimed" : "kit_on_cooldown", commandData.kitCooldownMap.get(ID)));
                return 0;
            }


            kit.giveKit(player);
            if(kit.cooldown > 0 || kit.oneTimeUse) {
                commandData.kitCooldownMap.put(ID, kit.cooldown);
                data.put(commandData);
            }
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
        }
        return 1;
    }
}