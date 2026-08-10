package su.nightexpress.excellenteconomy.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.data.DataQueries;
import su.nightexpress.nightcore.db.AbstractUserManager;

public class UserManager extends AbstractUserManager<ExcellentEconomyPlugin, CoinsUser> {

    private final DataHandler dataHandler;
    private final CurrencyRegistry registry;

    public UserManager(ExcellentEconomyPlugin plugin, CurrencyRegistry registry,
            DataHandler dataHandler) {
        super(plugin, dataHandler);
        this.dataHandler = dataHandler;
        this.registry = registry;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.dataHandler.addTableSync(this.dataHandler.getUsersTable(), resultSet -> {
            CoinsUser user = DataQueries.USER_LOADER.apply(resultSet);
            if (user != null) {
                this.handleSynchronization(user);
            }
        });
    }

    @Override

    public CoinsUser create(UUID uuid, String name) {
        long dateCreated = System.currentTimeMillis();

        UserBalance balance = new UserBalance();
        this.registry.getCurrencies().forEach(currency -> balance.set(currency, currency.getStartValue()));

        Map<String, UserCurrencySettings> settingsMap = new HashMap<>();
        boolean hiddenFromTops = false;

        return new CoinsUser(uuid, name, dateCreated, dateCreated, balance, settingsMap, hiddenFromTops);
    }

    public void handleSynchronization(CoinsUser fresh) {
        CoinsUser user = this.getLoaded(fresh.getId());
        if (user == null)
            return;

        for (Currency currency : this.registry.getCurrencies()) {
            if (!currency.isSynchronizable())
                continue;

            double balance = fresh.getBalance(currency);
            user.getBalance().set(currency, balance); // Bypass balance event call.
        }
    }
}
