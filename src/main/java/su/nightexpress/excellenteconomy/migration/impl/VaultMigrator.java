package su.nightexpress.excellenteconomy.migration.impl;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.hook.HookPlugin;
import su.nightexpress.excellenteconomy.migration.Migrator;

@SuppressWarnings("deprecation")
public class VaultMigrator extends Migrator {

    private final Economy economy;

    public VaultMigrator(ExcellentEconomyPlugin plugin, Economy economy) {
        super(plugin, HookPlugin.VAULT);
        this.economy = economy;
    }

    @Override
    public boolean canMigrate(Currency currency) {
        return !currency.isPrimary();
    }

    @Override

    public Map<OfflinePlayer, Double> getBalances(Currency currency) {
        Map<OfflinePlayer, Double> balances = new HashMap<>();

        for (OfflinePlayer offlinePlayer : this.plugin.getServer().getOfflinePlayers()) {
            try {
                balances.put(offlinePlayer, this.economy.getBalance(offlinePlayer));
            } catch (Exception exception) {
                this.plugin.error("Could not convert Vault <-> Economy balance for '" + offlinePlayer.getUniqueId()
                        + "'! See stacktrace for details:");
                exception.printStackTrace();
            }
        }

        return balances;
    }
}
