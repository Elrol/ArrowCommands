package dev.elrol.arrow.commands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.registries.CrateRegistry;
import dev.elrol.arrow.data.PlayerDataCore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ArrowCore.INSTANCE.getPlayerDataRegistry().getLoadedData().forEach((uuid, data) -> {
            final PlayerDataCore coreData = data.get(new PlayerDataCore());
            if(coreData.username == null) return;
            final String name = coreData.username.getString();
            builder.suggest(name);
        });
        return builder.buildFuture();
    }

}
