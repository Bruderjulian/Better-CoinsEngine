package su.nightexpress.excellenteconomy.currency.operation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class OperationContext {

    private final String name;
    private final CommandSender sender;
    private final EnumSet<NotificationTarget> notificationTargets;

    private OperationContext(final String name, final CommandSender sender) {
        this.notificationTargets = EnumSet.allOf(NotificationTarget.class);
        this.name = name;
        this.sender = sender;
    }

    public static OperationContext of(final CommandSender sender) {
        return new OperationContext(null, sender);
    }

    public static OperationContext custom(final String name) {
        return new OperationContext(name, null);
    }

    public static OperationContext console() {
        return new OperationContext(null, Bukkit.getConsoleSender());
    }

    public static OperationContext consoleQuiet() {
        return console().silent();
    }

    public OperationContext silent() {
        return this.silentFor(NotificationTarget.values());
    }

    public String getName() {
        return this.name == null ? sender.getName() : this.name;
    }

    public Optional<CommandSender> getBukkitSender() {
        return this.sender == null ? Optional.empty() : Optional.of(this.sender);
    }

    /**
     * Makes the operation silent for the specified targets.
     * 
     * @param targets The targets to disable notifications for.
     * @return The current context instance for method chaining.
     */

    public OperationContext silentFor(final NotificationTarget... targets) {
        Arrays.asList(targets).forEach(this.notificationTargets::remove);
        return this;
    }

    public OperationContext silentFor(final NotificationTarget target, final boolean flag) {
        if (flag)
            this.notificationTargets.remove(target);
        else
            this.notificationTargets.add(target);
        return this;
    }

    public boolean shouldNotify(final NotificationTarget target) {
        return this.notificationTargets.contains(target);
    }

    public boolean shouldNotifyLogger() {
        return this.shouldNotify(NotificationTarget.CONSOLE_LOGGER)
                || this.shouldNotify(NotificationTarget.FILE_LOGGER);
    }

}
