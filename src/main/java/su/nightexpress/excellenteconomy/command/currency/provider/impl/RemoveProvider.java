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
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class RemoveProvider extends CurrencyCommandProvider {

    public RemoveProvider(@NotNull ExcellentEconomyPlugin plugin, @NotNull CurrencyRegistry registry,
            @NotNull CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.REMOVE);
    }

    @Override
    public void buildRoot(@NotNull Currency currency, @NotNull HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull Currency currency, @NotNull LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_TAKE)
                .description(Lang.COMMAND_CURRENCY_TAKE_DESC)
                .withArguments(
                        Arguments.playerName(CommandArguments.PLAYER),
                        CommandArguments.amount())
                .withFlags(CommandArguments.FLAG_SILENT, CommandArguments.FLAG_SILENT_FEEDBACK)
                .executes((context, arguments) -> {
                    double amount = arguments.getDouble(CommandArguments.AMOUNT);
                    if (amount <= 0D)
                        return false;

                    String playerName = arguments.getString(CommandArguments.PLAYER);

                    this.plugin.getUserManager().manageUser(playerName, user -> {
                        if (user == null) {
                            context.errorBadPlayer();
                            return;
                        }

                        OperationContext operationContext = OperationContext.of(context.getSender())
                                .silentFor(NotificationTarget.CONSOLE_LOGGER)
                                .silentFor(NotificationTarget.USER, context.hasFlag(CommandArguments.FLAG_SILENT))
                                .silentFor(NotificationTarget.EXECUTOR,
                                        context.hasFlag(CommandArguments.FLAG_SILENT_FEEDBACK));

                        this.manager.remove(operationContext, user, currency, amount);
                    });
                    return true;
                });
    }

    @Override
    public boolean isAvailable(@NotNull Currency currency) {
        return true;
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("remove"), CommandVariant.disabled("takemoney"));
    }
}
