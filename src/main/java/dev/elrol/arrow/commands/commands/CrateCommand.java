package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.CrateSuggestionProvider;
import dev.elrol.arrow.commands.data.CrateData;
import dev.elrol.arrow.commands.menus.CrateMenu;
import dev.elrol.arrow.commands.registries.CrateRegistry;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CrateCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(CommandManager.literal("crate")
                .requires((source)->{
                    ServerPlayerEntity player = source.getPlayer();
                    if(source.isExecutedByPlayer() && player != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "crate").asBoolean() || player.hasPermissionLevel(4);
                    }
                    return true;
                })
                .then(argument("crate", StringArgumentType.string())
                        .suggests(new CrateSuggestionProvider())
                        .executes(this::oneArg)
                )
        );
    }

    private int oneArg(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = getPlayer(context);
        String crateID = StringArgumentType.getString(context, "crate");

        CrateData crate = CrateRegistry.get(crateID);
        if(player != null) {
            if (crate != null) {
                boolean flag = player.getInventory().contains(crate.crateKey);
                if (flag) {
                    int slot = player.getInventory().getSlotWithStack(crate.crateKey);
                    ArrowCommands.LOGGER.warn("Slot is: {}", slot);
                    player.getInventory().removeStack(slot, 1);

                    CrateMenu menu = (CrateMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("crate", player);
                    menu.setCrateID(crateID);
                    menu.open();
                    return 1;
                } else {
                    player.sendMessage(ModTranslations.err("no_key_1")
                            .append(crate.name)
                            .append(ModTranslations.err("no_key_2")));
                    return 0;
                }
            } else {
                player.sendMessage(ModTranslations.err("missing_crate", crateID));
            }
        }
        return 0;
    }
}
