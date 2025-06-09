package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.elrol.arrow.commands.ArrowCommands;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DiscordCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(CommandManager.literal("discord")
                .executes(context -> {
                    String link = ArrowCommands.CONFIG.discordLink;
                    context.getSource().sendMessage(ModTranslations.msg("discord_ip").formatted(Formatting.GREEN).append(Text.literal(link).setStyle(Style.EMPTY.withItalic(false).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)).withFormatting(Formatting.BLUE))));
                    return 1;
                }).requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "back").asBoolean();
                    }
                    return true;
                }));
    }
}
