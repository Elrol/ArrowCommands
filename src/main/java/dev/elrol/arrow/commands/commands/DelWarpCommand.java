package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.ServerDataCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DelWarpCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("delwarp")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes(DelWarpCommand::oneArgs)
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin", "delwarp").asBoolean();
                            }
                            return true;
                        })));
    }

    private static int oneArgs(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        ServerDataCommands data = ArrowCore.INSTANCE.getServerDataRegistry().get(new ServerDataCommands());
        if(data.removeWarp(name)) {
            context.getSource().sendMessage(ModTranslations.msg("warp_delete_1")
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(ModTranslations.msg("warp_delete_2")));
        } else {
            context.getSource().sendMessage(ModTranslations.err("warp_missing"));
        }
        return 1;
    }
}