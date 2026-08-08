package su.nightexpress.excellenteconomy.currency;

import java.nio.file.Path;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.currency.impl.AbstractCurrency;
import su.nightexpress.excellenteconomy.currency.impl.EconomyCurrency;
import su.nightexpress.excellenteconomy.currency.impl.NormalCurrency;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.user.UserManager;

public class CurrencyFactory {

    private CurrencyFactory() {
    }

    public static AbstractCurrency createEconomy(Path path,
            String id,
            CoinsEnginePlugin plugin,
            CurrencyManager currencyManager,
            DataHandler dataHandler,
            UserManager userManager) {
        return new EconomyCurrency(path, id, plugin, currencyManager, dataHandler, userManager);
    }

    public static AbstractCurrency createNormal(Path path, String id) {
        return new NormalCurrency(path, id);
    }
}
