package su.nightexpress.excellenteconomy.data.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.api.event.ChangeBalanceEvent;
import su.nightexpress.excellenteconomy.user.UserBalance;
import su.nightexpress.nightcore.db.AbstractUser;

public class CoinsUser extends AbstractUser {

    private final UserBalance balance;
    private final Map<String, CurrencySettings> settingsMap;

    private boolean hiddenFromTops;

    public CoinsUser(UUID uuid,
            String name,
            long dateCreated,
            long lastLogin,
            UserBalance balance,
            Map<String, CurrencySettings> settingsMap,
            boolean hiddenFromTops) {
        super(uuid, name, dateCreated, lastLogin);
        this.balance = balance;
        this.settingsMap = new HashMap<>(settingsMap);
        this.setHiddenFromTops(hiddenFromTops);
    }

    @Deprecated
    public Map<String, Double> getBalanceMap() {
        return this.balance.getBalanceMap();
    }

    public UserBalance getBalance() {
        return this.balance;
    }

    /**
     * Edits user's balance of specific currency and fires the ChangeBalanceEvent
     * event. If event was cancelled, the balance is set back to previous (old)
     * value.
     *
     * @param currency Currency to edit balance of.
     * @param consumer balance function.
     */
    public void editBalance(Currency currency, Consumer<UserBalance> consumer) {
        double oldBalance = this.getBalance(currency);

        consumer.accept(this.balance);

        ChangeBalanceEvent event = new ChangeBalanceEvent(this, currency, oldBalance, this.getBalance(currency));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            this.balance.set(currency, oldBalance);
        }
    }

    public void resetBalance(Collection<Currency> currencies) {
        currencies.forEach(this::resetBalance);
    }

    public void resetBalance(Currency currency) {
        this.editBalance(currency, balance -> balance.set(currency, currency.getStartValue()));
    }

    public boolean hasEnough(Currency currency, double amount) {
        return this.balance.has(currency, amount);
    }

    public double getBalance(Currency currency) {
        return this.balance.get(currency);
    }

    public void addBalance(Currency currency, double amount) {
        this.editBalance(currency, balance -> balance.add(currency, amount));
    }

    public void removeBalance(Currency currency, double amount) {
        this.editBalance(currency, lookup -> lookup.remove(currency, amount));
    }

    public void setBalance(Currency currency, double amount) {
        this.editBalance(currency, lookup -> lookup.set(currency, amount));
    }

    public Map<String, CurrencySettings> getSettingsMap() {
        return this.settingsMap;
    }

    public CurrencySettings getSettings(Currency currency) {
        return this.settingsMap.computeIfAbsent(currency.getId(), k -> CurrencySettings.create(currency));
    }

    public boolean isHiddenFromTops() {
        return this.hiddenFromTops;
    }

    public void setHiddenFromTops(boolean hiddenFromTops) {
        this.hiddenFromTops = hiddenFromTops;
    }
}
