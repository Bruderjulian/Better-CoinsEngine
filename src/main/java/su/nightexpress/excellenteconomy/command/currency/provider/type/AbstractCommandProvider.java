package su.nightexpress.excellenteconomy.command.currency.provider.type;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.command.currency.provider.CommandProvider;

public abstract class AbstractCommandProvider implements CommandProvider {

    protected final ExcellentEconomyPlugin plugin;
    protected final String name;

    public AbstractCommandProvider(@NotNull ExcellentEconomyPlugin plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }
}
