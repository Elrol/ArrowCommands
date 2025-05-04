package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.data.ServerDataCommands;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ListWarpCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(CommandManager.literal("listwarp")
                .executes(this::noArgs));

        redirect(dispatcher, "warplist");
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        MutableText text = ModTranslations.translate("arrow.message.warp_list").formatted(Formatting.DARK_AQUA, Formatting.BOLD);
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerDataCommands data = ArrowCore.INSTANCE.getServerDataRegistry().get(ServerDataCommands.class);
        data.getWarpsNames().forEach(warp -> {
            if(player != null && !PermUtils.hasPerm(player, "arrow.warp", warp).asBoolean()) return;
            text.append(Text.literal("\n" + warp).formatted(Formatting.AQUA));
        });

        context.getSource().sendMessage(text);
        return 1;
    }
}
