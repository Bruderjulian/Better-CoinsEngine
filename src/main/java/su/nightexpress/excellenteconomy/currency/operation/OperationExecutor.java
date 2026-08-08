package su.nightexpress.excellenteconomy.currency.operation;

import java.util.Optional;

import org.bukkit.command.CommandSender;

public interface OperationExecutor {

    String getName();

    Optional<CommandSender> getBukkitSender();

    static OperationExecutor of(CommandSender sender) {
        return new OperationExecutor() {

            @Override

            public String getName() {
                return sender.getName();
            }

            @Override

            public Optional<CommandSender> getBukkitSender() {
                return Optional.of(sender);
            }
        };
    }

    static OperationExecutor custom(String name) {
        return new OperationExecutor() {

            @Override

            public String getName() {
                return name;
            }

            @Override

            public Optional<CommandSender> getBukkitSender() {
                return Optional.empty();
            }
        };
    }
}
