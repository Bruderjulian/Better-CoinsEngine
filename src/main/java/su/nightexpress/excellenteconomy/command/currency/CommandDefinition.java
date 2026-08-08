package su.nightexpress.excellenteconomy.command.currency;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public record CommandDefinition(CommandVariant children, CommandVariant dedicated) implements Writeable {

    public static CommandDefinition read(FileConfig config, String path) {
        CommandVariant childVar = CommandVariant.read(config, path + ".Children");
        CommandVariant dedicVar = CommandVariant.read(config, path + ".Dedicated");

        return new CommandDefinition(childVar, dedicVar);
    }

    @Override
    public void write(FileConfig config, String path) {
        config.set(path + ".Children", this.children);
        config.set(path + ".Dedicated", this.dedicated);
    }
}
