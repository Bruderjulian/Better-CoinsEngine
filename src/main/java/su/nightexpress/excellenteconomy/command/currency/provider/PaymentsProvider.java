package su.nightexpress.excellenteconomy.command.currency.provider;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommandProvider;
import su.nightexpress.excellenteconomy.command.currency.ProviderNames;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class PaymentsProvider extends CurrencyCommandProvider {

    public PaymentsProvider(@NotNull final ExcellentEconomyPlugin plugin, @NotNull final CurrencyRegistry registry,
            @NotNull final CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.PAYMENTS);
    }

    @Override
    public void buildRoot(@NotNull final Currency currency, @NotNull final HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull final Currency currency, @NotNull final LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_PAYMENTS)
                .description(Lang.COMMAND_CURRENCY_PAYMENTS_DESC)
                .withArguments(Arguments.playerName(CommandArguments.PLAYER)
                        .permission(Perms.COMMAND_CURRENCY_PAYMENTS_OTHERS).optional())
                .withFlags(CommandArguments.FLAG_SILENT)
                .executes((context, arguments) -> {
                    final String name = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
                    final boolean silent = context.hasFlag(CommandArguments.FLAG_SILENT);

                    this.manager.togglePayments(context.getSender(), name, currency, silent);
                    return true;
                });
    }

    @Override
    public boolean isAvailable(@NotNull final Currency currency) {
        return currency.isTransferAllowed();
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("paytoggle"), CommandVariant.enabled("payments"));
    }

    @Override
    public void buildEco(@NotNull final Currency currency, @NotNull final LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_PAYMENTS)
                .description(Lang.COMMAND_CURRENCY_PAYMENTS_DESC)
                .withArguments(Arguments.playerName(CommandArguments.PLAYER))
                .withFlags(CommandArguments.FLAG_SILENT)
                .executes((context, arguments) -> {
                    final String name = arguments.getString(CommandArguments.PLAYER);
                    final boolean silent = context.hasFlag(CommandArguments.FLAG_SILENT);

                    this.manager.togglePayments(context.getSender(), name, currency, silent);
                    return true;
                });
    }
}
