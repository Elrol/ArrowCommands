package dev.elrol.arrow.commands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.registries.CrateRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

public class CrateSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {ServerPlayerEntity player = context.getSource().getPlayer();
        if(player != null) {
            for(String crate : CrateRegistry.getIDs()) {
                builder.suggest(crate);
            }
        }
        return builder.buildFuture();
    }

}
