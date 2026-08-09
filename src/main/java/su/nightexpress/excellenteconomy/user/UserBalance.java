package su.nightexpress.excellenteconomy.user;

import java.util.HashMap;
import java.util.Map;

import su.nightexpress.excellenteconomy.api.currency.Currency;

public class UserBalance {

    private final Map<String, Double> balanceMap;

    public UserBalance() {
        this(new HashMap<>());
    }

    public UserBalance(Map<String, Double> balanceMap) {
        this.balanceMap = balanceMap;
    }

    public Map<String, Double> getBalanceMap() {
        return this.balanceMap;
    }

    public void clear() {
        this.balanceMap.clear();
    }

    public void clear(Currency currency) {
        this.clear(currency.getId());
    }

    public void clear(String currencyId) {
        this.balanceMap.remove(currencyId);
    }

    public boolean has(Currency currency, double amount) {
        return this.get(currency) >= amount;
    }

    public double get(Currency currency) {
        return this.get(currency.getId());
    }

    public double get(String currencyId) {
        return this.balanceMap.getOrDefault(currencyId, 0D);
    }

    public void add(Currency currency, double amount) {
        this.add(currency.getId(), amount);
    }

    public void add(String currencyId, double amount) {
        this.set(currencyId, this.get(currencyId) + Math.abs(amount));
    }

    public void remove(Currency currency, double amount) {
        this.remove(currency.getId(), amount);
    }

    public void remove(String currencyId, double amount) {
        this.set(currencyId, this.get(currencyId) - Math.abs(amount));
    }

    public void set(Currency currency, double amount) {
        this.set(currency.getId(), currency.floorAndLimit(amount));
    }

    public void set(String currencyId, double amount) {
        this.balanceMap.put(currencyId, amount);
    }
}
