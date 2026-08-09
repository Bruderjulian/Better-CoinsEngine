package su.nightexpress.excellenteconomy.command.currency.provider;

import org.bukkit.entity.Player;

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

public class PayProvider extends CurrencyCommandProvider {

    public PayProvider(final ExcellentEconomyPlugin plugin, final CurrencyRegistry registry,
            final CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.PAY);
    }

    @Override
    public void buildRoot(final Currency currency, final HubNodeBuilder builder) {

    }

    @Override
    public void build(final Currency currency, final LiteralNodeBuilder builder) {
        builder
                .playerOnly()
                .permission(Perms.COMMAND_CURRENCY_PAY)
                .description(Lang.COMMAND_CURRENCY_PAY_DESC)
                .withArguments(
                        Arguments.playerName(CommandArguments.PLAYER),
                        CommandArguments.amount())
                .executes((context, arguments) -> {
                    final Player from = context.getPlayerOrThrow();
                    final String targetName = arguments.getString(CommandArguments.PLAYER);
                    final double amount = arguments.getDouble(CommandArguments.AMOUNT);

                    return this.manager.pay(from, targetName, currency, amount);
                });
    }

    @Override
    public boolean isAvailable(final Currency currency) {
        return currency.isTransferAllowed();
    }

    @Override

    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("pay"), CommandVariant.enabled("pay"));
    }
}
