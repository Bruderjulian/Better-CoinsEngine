package su.nightexpress.excellenteconomy.currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.nightcore.util.LowerCase;

public class CurrencyRegistry {

    // private final CoinsEnginePlugin plugin;
    private final Map<String, Currency> currencyMap;

    public CurrencyRegistry() {
        // this.plugin = plugin;
        this.currencyMap = new HashMap<>();
    }

    public void removeAll() {
        this.getCurrencies().forEach(this::remove);
    }

    /*
     * public void unregisterNormal() {
     * this.getCurrencies().stream().filter(Predicate.not(Currency::isPrimary)).
     * forEach(this::remove);
     * }
     */

    public void add(Currency currency) {
        this.currencyMap.put(currency.getId(), currency);

        currency.onRegister();
    }

    public Currency remove(Currency currency) {
        return this.remove(currency.getId());
    }

    public Currency remove(String id) {
        Currency currency = this.currencyMap.remove(LowerCase.INTERNAL.apply(id));
        if (currency != null) {
            currency.onUnregister();
        }

        return currency;
    }

    public Map<String, Currency> getCurrencyMap() {
        return this.currencyMap;
    }

    public boolean hasPrimary() {
        return this.findPrimary().isPresent();
    }

    public boolean isRegistered(String id) {
        return this.currencyMap.containsKey(LowerCase.INTERNAL.apply(id));
    }

    public Optional<Currency> findPrimary() {
        return this.currencyMap.values().stream().filter(Currency::isPrimary).findFirst();
    }

    public Currency getById(String id) {
        return this.currencyMap.get(LowerCase.INTERNAL.apply(id));
    }

    public Optional<Currency> byId(String id) {
        return Optional.ofNullable(this.getById(id));
    }

    public List<String> getCurrencyIds() {
        return new ArrayList<>(this.currencyMap.keySet());
    }

    public Collection<Currency> getCurrencies() {
        return new HashSet<>(this.currencyMap.values());
    }
}
