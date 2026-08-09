package su.nightexpress.excellenteconomy.command.currency;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public abstract class CommandProvider {

    protected final ExcellentEconomyPlugin plugin;
    protected final String name;

    public CommandProvider(final ExcellentEconomyPlugin plugin, final String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract void buildRoot(Currency currency, HubNodeBuilder builder);

    public abstract void build(Currency currency, LiteralNodeBuilder builder);

    public void buildHub(final Currency currency, final HubNodeBuilder builder) {
    }

    public void buildEco(final Currency currency, final LiteralNodeBuilder builder) {
    }

    public boolean isEcoCommand() {
        return false;
    }

    public boolean isHubCommand() {
        return false;
    }

    public String getEcoCommandName() {
        return null;
    }

    public abstract boolean isAvailable(Currency currency);

    public abstract CommandDefinition getDefaultDefinition();
}
