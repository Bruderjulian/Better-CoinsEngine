package su.nightexpress.excellenteconomy.tops.command;

import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.CommandNames;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.provider.ProviderNames;
import su.nightexpress.excellenteconomy.command.currency.provider.type.AbstractCommandProvider;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.tops.TopManager;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class TopCommandProvider extends AbstractCommandProvider {

    private final TopManager manager;

    public TopCommandProvider(@NotNull CoinsEnginePlugin plugin, @NotNull TopManager manager) {
        super(plugin, ProviderNames.TOP);
        this.manager = manager;
    }

    @Override
    public void buildRoot(@NotNull Currency currency, @NotNull HubNodeBuilder builder) {

    }

    @Override
    public void build(@NotNull Currency currency, @NotNull LiteralNodeBuilder builder) {
        builder
                .permission(Perms.COMMAND_CURRENCY_TOP)
                .description(Lang.COMMAND_CURRENCY_TOP_DESC)
                .withArguments(Arguments.integer(CommandArguments.AMOUNT, 1)
                        .localized(Lang.COMMAND_ARGUMENT_NAME_PAGE)
                        .suggestions((reader, context) -> IntStream.range(1, 11).boxed().map(String::valueOf).toList()))
                .executes((context, arguments) -> {
                    int page = arguments.getInt(CommandArguments.AMOUNT, 1);
                    this.manager.showLeaderboard(context.getSender(), currency, page);
                    return true;
                });
    }

    @Override
    public boolean isAvailable(@NotNull Currency currency) {
        return currency.isLeaderboardEnabled();
    }

    @Override
    @NotNull
    public CommandDefinition getDefaultDefinition() {
        return new CommandDefinition(CommandVariant.enabled(CommandNames.CURRENCY_TOP),
                CommandVariant.enabled("balancetop", "baltop"));
    }
}
