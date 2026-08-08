package su.nightexpress.excellenteconomy.command.currency.provider;

import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public interface CommandProvider {

    String getName();

    void buildRoot(Currency currency, HubNodeBuilder builder);

    void build(Currency currency, LiteralNodeBuilder builder);

    boolean isAvailable(Currency currency);

    CommandDefinition getDefaultDefinition();
}
