package su.nightexpress.excellenteconomy.command.currency.provider.impl;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.provider.ProviderNames;
import su.nightexpress.excellenteconomy.command.currency.provider.type.CurrencyCommandProvider;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;

public class BalanceProvider extends CurrencyCommandProvider {

    public BalanceProvider(@NotNull ExcellentEconomyPlugin plugin, @NotNull CurrencyRegistry registry,
            @NotNull CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.BALANCE);
    }

    @Override
    public void buildRoot(@NotNull Currency currency, @NotNull HubNodeBuilder builder) {
        builder.executes((context, arguments) -> this.showBalance(currency, context, arguments));
    }

    @Override
    public void build(@NotNull Currency currency, @NotNull LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_BALANCE)
                .description(Lang.COMMAND_CURRENCY_BALANCE_DESC)
                .withArguments(Arguments.playerName(CommandArguments.PLAYER).optional()
                        .permission(Perms.COMMAND_CURRENCY_BALANCE_OTHERS))
                .executes((context, arguments) -> this.showBalance(currency, context, arguments));
    }

    private boolean showBalance(@NotNull Currency currency, @NotNull CommandContext context,
            @NotNull ParsedArguments arguments) {
        String name = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
        this.manager.showBalance(context.getSender(), name, currency);
        return true;
    }

    @Override
    public boolean isAvailable(@NotNull Currency currency) {
        return true;
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("balance"), CommandVariant.enabled("balance", "bal"));
    }
}
