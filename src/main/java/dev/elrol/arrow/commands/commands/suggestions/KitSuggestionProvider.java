package dev.elrol.arrow.commands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.arrow.commands.data.KitData;
import dev.elrol.arrow.commands.registries.KitRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KitSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    final boolean showAll;

    public KitSuggestionProvider(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null || showAll) {
            for (Map.Entry<String, KitData> entry : KitRegistry.get().entrySet()) {
                if (entry.getValue().hasPermission(player) || showAll)
                    builder.suggest(entry.getKey());
            }
        }
        return builder.buildFuture();
    }

}
