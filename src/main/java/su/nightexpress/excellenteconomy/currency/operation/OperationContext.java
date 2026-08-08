package su.nightexpress.excellenteconomy.currency.operation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class OperationContext {

    private final OperationExecutor executor;
    private final EnumSet<NotificationTarget> notificationTargets;

    private OperationContext(OperationExecutor executor) {
        this.executor = executor;
        this.notificationTargets = EnumSet.allOf(NotificationTarget.class);
    }

    public static OperationContext of(OperationExecutor sender) {
        return new OperationContext(sender);
    }

    public static OperationContext of(CommandSender sender) {
        return of(OperationExecutor.of(sender));
    }

    public static OperationContext custom(String name) {
        return of(OperationExecutor.custom(name));
    }

    public static OperationContext console() {
        return of(Bukkit.getConsoleSender());
    }

    public static OperationContext consoleQuiet() {
        return console().silent();
    }

    public OperationContext silent() {
        return this.silentFor(NotificationTarget.values());
    }

    /**
     * Makes the operation silent for the specified targets.
     * 
     * @param targets The targets to disable notifications for.
     * @return The current context instance for method chaining.
     */

    public OperationContext silentFor(NotificationTarget... targets) {
        Arrays.asList(targets).forEach(this.notificationTargets::remove);
        return this;
    }

    public OperationContext silentFor(NotificationTarget target, boolean flag) {
        if (flag)
            this.notificationTargets.remove(target);
        else
            this.notificationTargets.add(target);
        return this;
    }

    public boolean shouldNotify(NotificationTarget target) {
        return this.notificationTargets.contains(target);
    }

    public boolean shouldNotifyLogger() {
        return this.shouldNotify(NotificationTarget.CONSOLE_LOGGER)
                || this.shouldNotify(NotificationTarget.FILE_LOGGER);
    }

    public OperationExecutor getExecutor() {
        return this.executor;
    }

    public Optional<CommandSender> getBukkitSender() {
        return this.executor.getBukkitSender();
    }
}
