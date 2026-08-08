package su.nightexpress.excellenteconomy.currency.impl;

import java.nio.file.Path;

public class NormalCurrency extends AbstractCurrency {

    public NormalCurrency(Path path, String id) {
        super(path, id);
    }

    @Override
    public void onRegister() {

    }

    @Override
    public void onUnregister() {

    }

    @Override
    public boolean isPrimary() {
        return false;
    }
}
