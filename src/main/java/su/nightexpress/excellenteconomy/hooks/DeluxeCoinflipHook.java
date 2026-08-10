package su.nightexpress.excellenteconomy.hooks;

import org.bukkit.OfflinePlayer;

import net.zithium.deluxecoinflip.api.DeluxeCoinflipAPI;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.user.CoinsUser;

public class DeluxeCoinflipHook {
    public static String NAME = "DeluxeCoinflip";

    public static void setup(ExcellentEconomyPlugin plugin) {
        DeluxeCoinflipAPI api = (DeluxeCoinflipAPI) plugin.getPluginManager().getPlugin(DeluxeCoinflipHook.NAME);
        if (api == null)
            return;

        plugin.getCurrencyRegistry().getCurrencies().forEach(currency -> {
            Provider provider = new Provider(plugin, currency);
            api.registerEconomyProvider(provider, plugin.getName());
        });
    }

    public static void shutdown() {

    }

    private static class Provider extends EconomyProvider {

        private final ExcellentEconomyPlugin plugin;
        private final CurrencyManager manager;
        private final Currency currency;

        public Provider(ExcellentEconomyPlugin plugin, Currency currency) {
            super("excellenteconomy_" + currency.getId());
            this.plugin = plugin;
            this.manager = plugin.getCurrencyManager();
            this.currency = currency;
        }

        @Override
        public void onEnable() {

        }

        @Override
        public String getDisplayName() {
            return this.currency.getName();
        }

        private CoinsUser getUser(OfflinePlayer offlinePlayer) {
            // Prefer loaded user to avoid main-thread DB access
            CoinsUser loaded = this.plugin.getUserManager().getLoaded(offlinePlayer.getUniqueId());
            if (loaded != null)
                return loaded;
            if (!org.bukkit.Bukkit.isPrimaryThread()) {
                return this.plugin.getUserManager().getOrFetch(offlinePlayer.getUniqueId());
            }
            return null;
        }

        @Override
        public double getBalance(OfflinePlayer offlinePlayer) {
            CoinsUser user = this.getUser(offlinePlayer);
            return user == null ? 0 : user.getBalance(this.currency);
        }

        @Override
        public void withdraw(OfflinePlayer offlinePlayer, double amount) {
            this.plugin.getUserManager().manageUser(offlinePlayer.getUniqueId(), user -> {
                if (user == null)
                    return;

                this.manager.remove(this.operationContext(), user, this.currency, amount);
            });
        }

        @Override
        public void deposit(OfflinePlayer offlinePlayer, double amount) {
            this.plugin.getUserManager().manageUser(offlinePlayer.getUniqueId(), user -> {
                if (user == null)
                    return;

                this.manager.give(this.operationContext(), user, this.currency, amount);
            });
        }

        private OperationContext operationContext() {
            return OperationContext.custom("DeluxeCoinflip").silentFor(NotificationTarget.USER,
                    NotificationTarget.EXECUTOR, NotificationTarget.CONSOLE_LOGGER);
        }
    }
}
