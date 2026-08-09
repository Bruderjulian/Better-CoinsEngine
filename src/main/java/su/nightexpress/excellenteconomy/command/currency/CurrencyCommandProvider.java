package su.nightexpress.excellenteconomy.command.currency;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public abstract class CurrencyCommandProvider extends CommandProvider {

    protected final CurrencyRegistry registry;
    protected final CurrencyManager manager;

    public CurrencyCommandProvider(ExcellentEconomyPlugin plugin, CurrencyRegistry registry, CurrencyManager manager,
            String name) {
        super(plugin, name);
        this.registry = registry;
        this.manager = manager;
    }

    @Override
    public void buildEco(Currency currency, LiteralNodeBuilder builder) {
        if (this.isEcoCommand()) {
            this.build(currency, builder);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }
}
