package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.CurrencySuggestionProvider;
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;

public class BalanceCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        root = commandDispatcher.register(literal("balance")
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "balance").asBoolean();
                            }
                            return true;
                        })
                        .executes(this::noArgs)
                        .then(argument("currency", StringArgumentType.string())
                                .suggests(new CurrencySuggestionProvider())
                                .executes(this::oneArg)
                        )
                );
        commandDispatcher.register(literal("bal")
                .requires((source)->{
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "balance").asBoolean();
                    }
                    return true;
                })
                .executes(this::noArgs)
                .then(argument("currency", StringArgumentType.string())
                        .suggests(new CurrencySuggestionProvider())
                        .executes(this::oneArg)
                )
        );
    }

    private int noArgs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(context.getSource().isExecutedByPlayer()) {
            IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
            sendBalance(context.getSource().getPlayerOrThrow(), economyRegistry.getPrimary());
        } else {
            context.getSource().sendMessage(ModTranslations.err("not_player"));
            return 0;
        }
        return 1;
    }

    private int oneArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        String id = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(id);

        if(currency == null) {

        } else {
            if(context.getSource().isExecutedByPlayer()) {
                sendBalance(context.getSource().getPlayerOrThrow(), currency);
            } else {
                context.getSource().sendMessage(ModTranslations.err("not_player"));
                return 0;
            }
        }

        return 1;
    }

    private void sendBalance(ServerPlayerEntity player, Currency currency) {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        BigDecimal bal = economyRegistry.getBal(player.getUuid(), currency);
        player.sendMessage(ModTranslations.msg("balance_1")
                .append(economyRegistry.getAmount(bal, currency))
                .append(ModTranslations.msg("balance_2")));
    }
}
