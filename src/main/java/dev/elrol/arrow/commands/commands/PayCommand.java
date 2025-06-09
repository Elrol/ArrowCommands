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
import dev.elrol.arrow.data.Currency;
import dev.elrol.arrow.libs.ModTranslations;
import dev.elrol.arrow.libs.PermUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;

public class PayCommand extends _CommandBase {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        root = commandDispatcher.register(literal("pay")
                        .requires((source)->{
                            if(source.isExecutedByPlayer() && source.getPlayer() != null) {
                                return PermUtils.hasPerm(source.getPlayer(), "arrow.command", "pay").asBoolean();
                            }
                            return true;
                        })
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .then(argument("currency", StringArgumentType.string())
                                                .suggests(new CurrencySuggestionProvider())
                                                .executes(this::threeArg)
                                        )
                                )
                        )

                );
    }

    private int threeArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        BigDecimal amount = BigDecimal.valueOf(IntegerArgumentType.getInteger(context, "amount"));

        String curID = StringArgumentType.getString(context, "currency");
        Currency currency = economyRegistry.getCurrency(curID);

        if(target == null) {
            context.getSource().sendMessage(ModTranslations.err("other_player_missing"));
            return 0;
        }

        if(currency != null) {
            if(context.getSource().isExecutedByPlayer()) {
                ServerPlayerEntity player = getPlayer(context);

                IEconomyRegistry econRegistry = ArrowCore.INSTANCE.getEconomyRegistry();
                BigDecimal sourceBalance = econRegistry.getBal(player.getUuid(), currency);

                if(sourceBalance.compareTo(amount) >= 0) {
                    String formatedAmount = economyRegistry.formatAmount(amount, currency);

                    econRegistry.withdraw(player.getUuid(), amount);
                    econRegistry.deposit(target.getUuid(), amount);

                    target.sendMessage(ModTranslations.msg("received_money", formatedAmount, player.getName()));
                    player.sendMessage(ModTranslations.msg("sent_money", formatedAmount, target.getName()));
                } else {
                    player.sendMessage(ModTranslations.err("not_enough_money", currency.getPlural()));
                    return 0;
                }
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
