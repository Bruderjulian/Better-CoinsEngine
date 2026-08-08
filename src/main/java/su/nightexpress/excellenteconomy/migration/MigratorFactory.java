package su.nightexpress.excellenteconomy.migration;

import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;
import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.migration.impl.VaultMigrator;
import su.nightexpress.nightcore.util.ServerUtils;

public class MigratorFactory {

    public static Migrator forVault(@NotNull CoinsEnginePlugin plugin) {
        @SuppressWarnings("deprecation")
        Economy economy = ServerUtils.serviceProvider(Economy.class).orElse(null);
        return economy == null ? null : new VaultMigrator(plugin, economy);
    }
}
