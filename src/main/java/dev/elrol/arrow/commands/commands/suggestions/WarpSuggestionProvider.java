package dev.elrol.arrow.commands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.data.ServerDataCommands;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

public class WarpSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if(player != null) {
            ServerDataCommands commandData = ArrowCore.INSTANCE.getServerDataRegistry().get(new ServerDataCommands());
            for(String warp : commandData.getWarpsNames()) {
                if(PermUtils.hasPerm(player, "arrow.warp", warp).asBoolean()) {
                    builder.suggest(warp);
                }
            }
        }
        return builder.buildFuture();
    }

}
