package su.nightexpress.excellenteconomy.command;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommands;
import su.nightexpress.excellenteconomy.command.plugin.PluginCommands;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.manager.SimpleManager;

public class CommandManager extends SimpleManager<ExcellentEconomyPlugin> {

    private final PluginCommands pluginCommands;
    private final CurrencyCommands currencyCommands;

    public CommandManager(ExcellentEconomyPlugin plugin, CurrencyRegistry currencyRegistry,
            CurrencyManager currencyManager) {
        super(plugin);
        this.pluginCommands = new PluginCommands(plugin, currencyManager);
        this.currencyCommands = new CurrencyCommands(plugin, currencyRegistry, currencyManager);
    }

    @Override
    protected void onLoad() {
        this.pluginCommands.setup();
        this.currencyCommands.setup();
    }

    @Override
    protected void onShutdown() {
        this.pluginCommands.shutdown();
        this.currencyCommands.shutdown();
    }

    public PluginCommands getPluginCommands() {
        return this.pluginCommands;
    }

    public CurrencyCommands getCurrencyCommands() {
        return this.currencyCommands;
    }
}
