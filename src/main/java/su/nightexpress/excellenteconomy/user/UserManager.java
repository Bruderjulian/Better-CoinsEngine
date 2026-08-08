package su.nightexpress.excellenteconomy.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.data.DataQueries;
import su.nightexpress.excellenteconomy.data.impl.CoinsUser;
import su.nightexpress.excellenteconomy.data.impl.CurrencySettings;
import su.nightexpress.nightcore.db.AbstractUserManager;

public class UserManager extends AbstractUserManager<ExcellentEconomyPlugin, CoinsUser> {

    private final DataHandler dataHandler;
    private final CurrencyRegistry registry;

    public UserManager(@NotNull ExcellentEconomyPlugin plugin, @NotNull CurrencyRegistry registry,
            @NotNull DataHandler dataHandler) {
        super(plugin, dataHandler);
        this.dataHandler = dataHandler;
        this.registry = registry;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.dataHandler.addTableSync(this.dataHandler.getUsersTable(), resultSet -> {
            CoinsUser user = DataQueries.USER_LOADER.apply(resultSet);
            this.handleSynchronization(user);
        });
    }

    @Override
    @NotNull
    public CoinsUser create(@NotNull UUID uuid, @NotNull String name) {
        long dateCreated = System.currentTimeMillis();

        UserBalance balance = new UserBalance();
        this.registry.getCurrencies().forEach(currency -> balance.set(currency, currency.getStartValue()));

        Map<String, CurrencySettings> settingsMap = new HashMap<>();
        boolean hiddenFromTops = false;

        return new CoinsUser(uuid, name, dateCreated, dateCreated, balance, settingsMap, hiddenFromTops);
    }

    public void handleSynchronization(@NotNull CoinsUser fresh) {
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
