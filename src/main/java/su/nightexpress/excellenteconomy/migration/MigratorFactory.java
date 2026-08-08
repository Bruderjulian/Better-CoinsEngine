package su.nightexpress.excellenteconomy.migration;

import net.milkbowl.vault.economy.Economy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.migration.impl.VaultMigrator;
import su.nightexpress.nightcore.util.ServerUtils;

public class MigratorFactory {

    @Nullable
    public static Migrator forVault(@NotNull CoinsEnginePlugin plugin) {
        Economy economy = ServerUtils.serviceProvider(Economy.class).orElse(null);
        return economy == null ? null : new VaultMigrator(plugin, economy);
    }
}
