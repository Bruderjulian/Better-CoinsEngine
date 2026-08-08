package su.nightexpress.excellenteconomy.command;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommands;
import su.nightexpress.excellenteconomy.command.plugin.PluginCommands;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.manager.SimpleManager;

public class CommandManager extends SimpleManager<CoinsEnginePlugin> {

    private final PluginCommands pluginCommands;
    private final CurrencyCommands currencyCommands;

    public CommandManager(@NotNull CoinsEnginePlugin plugin, @NotNull CurrencyRegistry currencyRegistry, @NotNull CurrencyManager currencyManager) {
        super(plugin);
        this.pluginCommands = new PluginCommands(plugin, currencyRegistry, currencyManager);
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

    @NotNull
    public PluginCommands getPluginCommands() {
        return this.pluginCommands;
    }

    @NotNull
    public CurrencyCommands getCurrencyCommands() {
        return this.currencyCommands;
    }
}
