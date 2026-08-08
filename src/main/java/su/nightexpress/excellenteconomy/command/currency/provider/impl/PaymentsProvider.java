package su.nightexpress.excellenteconomy.command.currency.provider.impl;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
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

public class PaymentsProvider extends CurrencyCommandProvider {

    public PaymentsProvider(@NotNull CoinsEnginePlugin plugin, @NotNull CurrencyRegistry registry, @NotNull CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.PAYMENTS);
    }

    @Override
    public void buildRoot(@NotNull Currency currency, @NotNull HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull Currency currency, @NotNull LiteralNodeBuilder builder) {
        builder
            .permission(Perms.COMMAND_CURRENCY_PAYMENTS)
            .description(Lang.COMMAND_CURRENCY_PAYMENTS_DESC)
            .withArguments(Arguments.playerName(CommandArguments.PLAYER).permission(Perms.COMMAND_CURRENCY_PAYMENTS_OTHERS).optional())
            .withFlags(CommandArguments.FLAG_SILENT)
            .executes((context, arguments) -> {
                String name = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
                boolean silent = context.hasFlag(CommandArguments.FLAG_SILENT);

                this.manager.togglePayments(context.getSender(), name, currency, silent);
                return true;
            });
    }

    @Override
    public boolean isAvailable(@NotNull Currency currency) {
        return currency.isTransferAllowed();
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("payments"), CommandVariant.enabled("paytoggle", "payments"));
    }
}
