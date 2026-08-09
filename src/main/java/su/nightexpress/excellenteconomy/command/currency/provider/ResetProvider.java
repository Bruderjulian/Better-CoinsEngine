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
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class ResetProvider extends CurrencyCommandProvider {

    public ResetProvider(@NotNull final ExcellentEconomyPlugin plugin, @NotNull final CurrencyRegistry registry,
            @NotNull final CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.RESET);
    }

    @Override
    public void buildRoot(@NotNull final Currency currency, @NotNull final HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull final Currency currency, @NotNull final LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_RESET)
                .description(Lang.COMMAND_CURRENCY_RESET_DESC)
                .withArguments(
                        Arguments.playerName(CommandArguments.PLAYER),
                        CommandArguments.currency(this.registry).optional())
                .withFlags(CommandArguments.FLAG_SILENT, CommandArguments.FLAG_SILENT_FEEDBACK)
                .executes((context, arguments) -> {
                    this.plugin.getUserManager().manageUser(arguments.getString(CommandArguments.PLAYER), user -> {
                        if (user == null) {
                            context.errorBadPlayer();
                            return;
                        }

                        final OperationContext operationContext = OperationContext.of(context.getSender())
                                .silentFor(NotificationTarget.USER, context.hasFlag(CommandArguments.FLAG_SILENT))
                                .silentFor(NotificationTarget.EXECUTOR,
                                        context.hasFlag(CommandArguments.FLAG_SILENT_FEEDBACK));

                        this.manager.reset(operationContext, user,
                                arguments.get(CommandArguments.CURRENCY, Currency.class));
                    });
                    return true;
                });
    }

    @Override
    public boolean isAvailable(@NotNull final Currency currency) {
        return true;
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("reset"), CommandVariant.disabled("ecoreset"));
    }
}
