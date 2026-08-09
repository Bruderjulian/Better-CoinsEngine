package su.nightexpress.excellenteconomy.migration;

import net.milkbowl.vault.economy.Economy;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.migration.impl.VaultMigrator;
import su.nightexpress.nightcore.util.ServerUtils;

public class MigratorFactory {

    public static Migrator forVault(ExcellentEconomyPlugin plugin) {
        @SuppressWarnings("deprecation")
        Economy economy = ServerUtils.serviceProvider(Economy.class).orElse(null);
        return economy == null ? null : new VaultMigrator(plugin, economy);
    }
}
