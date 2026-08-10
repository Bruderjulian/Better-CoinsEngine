package su.nightexpress.excellenteconomy.user;

import su.nightexpress.excellenteconomy.api.currency.Currency;

public class UserCurrencySettings {

    private boolean paymentsEnabled;

    public UserCurrencySettings(boolean paymentsEnabled) {
        this.setPaymentsEnabled(paymentsEnabled);
    }

    public static UserCurrencySettings create(Currency currency) {
        return new UserCurrencySettings(true);
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
    }
}
