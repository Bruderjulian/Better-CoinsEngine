package su.nightexpress.excellenteconomy.command.currency.provider.type;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;

public abstract class CurrencyCommandProvider extends AbstractCommandProvider {

    protected final CurrencyRegistry registry;
    protected final CurrencyManager manager;

    public CurrencyCommandProvider(@NotNull CoinsEnginePlugin plugin, @NotNull CurrencyRegistry registry, @NotNull CurrencyManager manager, @NotNull String name) {
        super(plugin, name);
        this.registry = registry;
        this.manager = manager;
    }
}
