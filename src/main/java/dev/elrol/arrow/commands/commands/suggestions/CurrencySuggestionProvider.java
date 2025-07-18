package dev.elrol.arrow.commands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.registries.CrateRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CurrencySuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        Set<String> currencies = economyRegistry.getCurrencies().keySet();

        String input = context.getInput();
        for(String currency : currencies) {
            if(input.isEmpty() || currency.startsWith(input)) {
                builder.suggest(currency);
            }
        }

        return builder.buildFuture();
    }

}
