package su.nightexpress.excellenteconomy.data.impl;

import su.nightexpress.excellenteconomy.api.currency.Currency;

public class CurrencySettings {

    private boolean paymentsEnabled;

    public CurrencySettings(boolean paymentsEnabled) {
        this.setPaymentsEnabled(paymentsEnabled);
    }

    public static CurrencySettings create(Currency currency) {
        return new CurrencySettings(true);
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
    }
}
