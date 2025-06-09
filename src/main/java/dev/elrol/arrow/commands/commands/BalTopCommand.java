package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.api.registries.IPlayerDataRegistry;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.CurrencySuggestionProvider;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BalTopCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        root = dispatcher.register(
                literal("baltop")
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "baltop").asBoolean();
                            }
                            return true;
                        })
                        .executes(this::noArgs)
                        .then(
                                argument("currency", StringArgumentType.string())
                                        .suggests(new CurrencySuggestionProvider())
                                        .executes(this::oneArg)
                        )
        );
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        sendBalances(context.getSource(), ArrowCore.INSTANCE.getEconomyRegistry().getPrimary());
        return 1;
    }

    private int oneArg(CommandContext<ServerCommandSource> context) {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        String id = StringArgumentType.getString(context, "currency");

        Currency currency = economyRegistry.getCurrency(id);
        if(currency == null) currency = economyRegistry.getPrimary();

        sendBalances(context.getSource(), currency);
        return 1;
    }

    private void sendBalances(ServerCommandSource source, Currency currency) {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        IPlayerDataRegistry playerDataRegistry = ArrowCore.INSTANCE.getPlayerDataRegistry();

        List<UUID> uuidList = economyRegistry.getTopBalances(currency);
        MutableText text = Text.literal("Top Balances:").formatted(Formatting.GREEN);

        int place = 1;
        for (UUID uuid : uuidList) {
            BigDecimal bal = economyRegistry.getBal(uuid, currency);
            PlayerDataCore coreData = playerDataRegistry.getPlayerData(uuid).get(new PlayerDataCore());
            MutableText name = coreData.username.equals(Text.empty()) ? Text.literal(uuid.toString()) : MutableText.of(coreData.username.getContent());

            text.append("\n");
            text.append(Text.literal("  [").formatted(Formatting.DARK_GRAY));
            text.append(Text.literal(String.valueOf(place)).formatted(Formatting.GRAY));
            text.append(Text.literal(place < 10 ? "]  " : "] ").formatted(Formatting.DARK_GRAY));
            text.append(name.formatted(Formatting.DARK_GREEN));
            text.append(": ").append(economyRegistry.getAmount(bal, currency));

            place++;
        }

        source.sendMessage(text);
    }
}
