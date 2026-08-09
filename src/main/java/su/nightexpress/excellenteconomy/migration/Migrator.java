package su.nightexpress.excellenteconomy.migration;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;

public abstract class Migrator {

    protected final ExcellentEconomyPlugin plugin;
    protected final String name;

    public Migrator(ExcellentEconomyPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Plugin getBackend() {
        return this.plugin.getPluginManager().getPlugin(this.name);
    }

    public abstract boolean canMigrate(Currency currency);

    public abstract Map<OfflinePlayer, Double> getBalances(Currency currency);
}
