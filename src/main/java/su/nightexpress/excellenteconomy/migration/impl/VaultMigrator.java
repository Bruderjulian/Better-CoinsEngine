package su.nightexpress.excellenteconomy.migration.impl;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.hook.HookPlugin;
import su.nightexpress.excellenteconomy.migration.Migrator;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.util.HashMap;
import java.util.Map;

public class VaultMigrator extends Migrator {

    private final Economy economy;

    public VaultMigrator(@NotNull CoinsEnginePlugin plugin, @NotNull Economy economy) {
        super(plugin, HookPlugin.VAULT);
        this.economy = economy;
    }

    @Override
    public boolean canMigrate(@NotNull Currency currency) {
        return !currency.isPrimary();
    }

    @Override
    @NotNull
    public Map<OfflinePlayer, Double> getBalances(@NotNull Currency currency) {
        Map<OfflinePlayer, Double> balances = new HashMap<>();

        for (OfflinePlayer offlinePlayer : this.plugin.getServer().getOfflinePlayers()) {
            try {
                balances.put(offlinePlayer, this.economy.getBalance(offlinePlayer));
            }
            catch (Exception exception) {
                this.plugin.error("Could not convert Vault <-> Economy balance for '" + offlinePlayer.getUniqueId() + "'! See stacktrace for details:");
                exception.printStackTrace();
            }
        }

        return balances;
    }
}
