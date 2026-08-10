package su.nightexpress.excellenteconomy.command.currency.provider;

import java.util.stream.IntStream;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandProvider;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.ProviderNames;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.tops.TopManager;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class TopCommandProvider extends CommandProvider {

    private final TopManager manager;

    public TopCommandProvider(final ExcellentEconomyPlugin plugin, final TopManager manager) {
        super(plugin, ProviderNames.TOP);
        this.manager = manager;
    }

    @Override
    public void buildRoot(final Currency currency, final HubNodeBuilder builder) {

    }

    @Override
    public void build(final Currency currency, final LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_TOP)
                .description(Lang.COMMAND_CURRENCY_TOP_DESC)
                .withArguments(Arguments.integer(CommandArguments.AMOUNT, 1)
                        .localized(Lang.COMMAND_ARGUMENT_NAME_PAGE)
                        .suggestions((reader, context) -> IntStream.range(1, 11).boxed().map(String::valueOf).toList()))
                .executes((context, arguments) -> {
                    final int page = arguments.getInt(CommandArguments.AMOUNT, 1);
                    this.manager.showLeaderboard(context.getSender(), currency, page);
                    return true;
                });
    }

    @Override
    public boolean isAvailable(final Currency currency) {
        return currency.isLeaderboardEnabled();
    }

    @Override

    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled("top"),
                CommandVariant.enabled("balancetop", "baltop"));
    }
}
