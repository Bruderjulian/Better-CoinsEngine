package su.nightexpress.excellenteconomy.command.currency.provider;

import org.bukkit.entity.Player;
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

public class SendProvider extends CurrencyCommandProvider {

    public SendProvider(@NotNull final ExcellentEconomyPlugin plugin, @NotNull final CurrencyRegistry registry,
            @NotNull final CurrencyManager manager) {
        super(plugin, registry, manager, ProviderNames.PAY);
    }

    @Override
    public void buildRoot(@NotNull final Currency currency, @NotNull final HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull final Currency currency, @NotNull final LiteralNodeBuilder builder) {
        builder
                .playerOnly()
                .permission(Perms.COMMAND_CURRENCY_SEND)
                .description(Lang.COMMAND_CURRENCY_SEND_DESC)
                .withArguments(
                        Arguments.playerName(CommandArguments.PLAYER),
                        CommandArguments.amount())
                .executes((context, arguments) -> {
                    final Player from = context.getPlayerOrThrow();
                    final String targetName = arguments.getString(CommandArguments.PLAYER);
                    final double amount = arguments.getDouble(CommandArguments.AMOUNT);

                    return this.manager.send(from, targetName, currency, amount);
                });
    }

    @Override
    public boolean isAvailable(@NotNull final Currency currency) {
        return currency.isTransferAllowed();
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("pay"), CommandVariant.enabled("pay"));
    }
}
