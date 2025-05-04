package dev.elrol.arrow.commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.registries.IEconomyRegistry;
import dev.elrol.arrow.commands._CommandBase;
import dev.elrol.arrow.commands.commands.suggestions.CurrencySuggestionProvider;
import dev.elrol.arrow.data.Account;
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.util.Collection;

public class EcoCommand extends _CommandBase {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        root = commandDispatcher.register(literal("arroweconomy")
                .requires(source -> {
                    if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                        return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin", "eco").asBoolean();
                    }
                    return true;
                })
                .executes(this::noArgs)
                .then(literal("set")
                        .requires(source -> {
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin.eco", "set").asBoolean();
                            }
                            return true;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                    .then(argument("currency", StringArgumentType.string())
                                            .suggests(new CurrencySuggestionProvider())
                                            .executes(this::set))))
                )
                .then(literal("add")
                        .requires(source -> {
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin.eco", "add").asBoolean();
                            }
                            return true;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .then(argument("currency", StringArgumentType.string())
                                                .suggests(new CurrencySuggestionProvider())
                                                .executes(this::add))))
                )
                .then(literal("remove")
                        .requires(source -> {
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin.eco", "remove").asBoolean();
                            }
                            return true;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .then(argument("currency", StringArgumentType.string())
                                                .suggests(new CurrencySuggestionProvider())
                                                .executes(this::remove))))
                ).then(literal("balance")
                        .requires(source -> {
                            if(source.isExecutedByPlayer() && source.getPlayer() != null)
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin.eco", "balance").asBoolean();
                            return true;
                        })
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("currency", StringArgumentType.string())
                                        .suggests(new CurrencySuggestionProvider())
                                        .executes(this::balance)
                                )
                        )
                ).then(literal("balances")
                        .requires(source -> {
                            if(source.isExecutedByPlayer() && source.getPlayer() != null)
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command.admin.eco", "balances").asBoolean();
                            return true;
                        })
                        .then(argument("player", EntityArgumentType.player())
                                .executes(this::balances)
                        )
                )
        );

        redirect(commandDispatcher, "economy");
        redirect(commandDispatcher, "eco");
        redirect(commandDispatcher, "econ");
    }

    private int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.literal("""
                /eco set <player> <balance> [currency]
                /eco add <player> <amount> [currency]
                /eco remove <player> <amount> [currency]
                /eco balance <player> [currency]
                /eco balances <player>
                """).formatted(Formatting.GREEN));
        return 1;
    }

    private int balances(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = getPlayer(context);
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        MutableText msg = player.getName().copy().append("'s Balances: ");

        economyRegistry.getCurrencies().forEach((id, currency) -> {
            BigDecimal bal = economyRegistry.getBal(player.getUuid(), currency);
            msg.append("\n    ").append(economyRegistry.getAmount(bal, currency));
        });

        sender.sendMessage(msg);

        return 1;
    }

    private int balance(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = getPlayer(context);
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        String id = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(id);

        BigDecimal bal = economyRegistry.getBal(player.getUuid(), currency);
        MutableText text = economyRegistry.getAmount(bal, currency);

        sender.sendMessage(player.getName().copy().append("'s Balance: ").append(text));
        return 1;
    }

    private int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = getPlayer(context);
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        String id = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(id);
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if(currency == null) return 0;

        if(sender != null) {
            players.forEach(player -> economyRegistry.changeAccount(player.getUuid(), currency, (account) -> {
                account.setBalance(BigDecimal.valueOf(amount));
                String formattedAmount = economyRegistry.formatAmount(amount, currency);

                player.sendMessage(ModTranslations.msg("new_balance_1")
                                .append(economyRegistry.getAmount(amount, currency))
                                .append(ModTranslations.msg("new_balance_2")));

                sender.sendMessage(ModTranslations.msg("gave_money", formattedAmount, player.getName().getString()));
                return account;
            }));
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
            return 0;
        }
        return 1;
    }

    private int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = getPlayer(context);
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        String id = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(id);
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if(currency == null) return 0;

        if(sender != null) {
            players.forEach(player -> economyRegistry.changeAccount(player.getUuid(), currency, (account) -> {
                account.deposit(BigDecimal.valueOf(amount));
                String formattedAmount = economyRegistry.formatAmount(amount, currency);

                player.sendMessage(ModTranslations.msg("got_money_1")
                        .append(economyRegistry.getAmount(amount, currency))
                        .append(ModTranslations.msg("got_money_2"))
                        .append(economyRegistry.getAmount(account.getBalance()))
                        .append(ModTranslations.msg("got_money_3"))
                );

                sender.sendMessage(ModTranslations.msg("gave_money", formattedAmount, player.getName().getString()));
                return account;
            }));
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
            return 0;
        }
        return 1;
    }

    private int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = getPlayer(context);
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
        String id = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(id);
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if(currency == null) return 0;

        if(sender != null) {
            players.forEach(player -> economyRegistry.changeAccount(player.getUuid(), currency, (account) -> {
                account.withdraw(BigDecimal.valueOf(amount));
                String formattedAmount = economyRegistry.formatAmount(amount, currency);
                player.sendMessage(ModTranslations.err("lost_money_1")
                        .append(economyRegistry.getAmount(amount))
                        .append(ModTranslations.err("lost_money_2"))
                        .append(economyRegistry.getAmount(account.getBalance()))
                        .append(ModTranslations.err("lost_money_3"))
                );
                        //, formattedAmount, economyRegistry.formatAmount(account.getBalance(), currency)));

                sender.sendMessage(ModTranslations.msg("removed_money", formattedAmount, player.getName().getString()));
                return account;
            }));
        } else {
            context.getSource().sendError(ModTranslations.err("not_player"));
            return 0;
        }
        return 1;
    }
}
