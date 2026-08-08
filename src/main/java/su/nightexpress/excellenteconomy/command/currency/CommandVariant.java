package su.nightexpress.excellenteconomy.command.currency;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public record CommandVariant(boolean enabled, String[] aliases) implements Writeable {

    public static CommandVariant read(FileConfig config, String path) {
        boolean enabled = ConfigValue.create(path + ".Enabled", false).read(config);
        String[] aliases = config.getStringArray(path + ".Aliases");

        return new CommandVariant(enabled, aliases);
    }

    public static CommandVariant enabled(String... aliases) {
        return new CommandVariant(true, aliases);
    }

    public static CommandVariant disabled(String... aliases) {
        return new CommandVariant(false, aliases);
    }

    @Override
    public void write(FileConfig config, String path) {
        config.set(path + ".Enabled", this.enabled);
        config.setStringArray(path + ".Aliases", this.aliases);
    }
}
