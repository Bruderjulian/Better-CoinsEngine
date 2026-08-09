package su.nightexpress.excellenteconomy.command.currency.provider;

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
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class SetProvider extends CurrencyCommandProvider {

    public SetProvider(final ExcellentEconomyPlugin plugin, final CurrencyRegistry registry,
            final CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.SET);
    }

    @Override
    public void buildRoot(final Currency currency, final HubNodeBuilder builder) {

    }

    @Override
    public void build(final Currency currency, final LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_SET)
                .description(Lang.COMMAND_CURRENCY_SET_DESC)
                .withArguments(
                        Arguments.playerName(CommandArguments.PLAYER),
                        CommandArguments.amount())
                .withFlags(CommandArguments.FLAG_SILENT, CommandArguments.FLAG_SILENT_FEEDBACK)
                .executes((context, arguments) -> {
                    final double amount = Math.max(0, arguments.getDouble(CommandArguments.AMOUNT));
                    final String playerName = arguments.getString(CommandArguments.PLAYER);

                    this.plugin.getUserManager().manageUser(playerName, user -> {
                        if (user == null) {
                            context.errorBadPlayer();
                            return;
                        }

                        final OperationContext operationContext = OperationContext.of(context.getSender())
                                .silentFor(NotificationTarget.CONSOLE_LOGGER)
                                .silentFor(NotificationTarget.USER, context.hasFlag(CommandArguments.FLAG_SILENT))
                                .silentFor(NotificationTarget.EXECUTOR,
                                        context.hasFlag(CommandArguments.FLAG_SILENT_FEEDBACK));

                        this.manager.set(operationContext, user, currency, amount);
                    });
                    return true;
                });
    }

    @Override
    public boolean isAvailable(final Currency currency) {
        return true;
    }

    @Override
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("set"), CommandVariant.disabled("setmoney"));
    }
}
