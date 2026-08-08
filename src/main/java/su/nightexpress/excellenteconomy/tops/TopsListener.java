package su.nightexpress.excellenteconomy.tops;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class TopsListener extends AbstractListener<ExcellentEconomyPlugin> {

    private final TopManager manager;

    public TopsListener(@NotNull ExcellentEconomyPlugin plugin, @NotNull TopManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.manager.hideFromTops(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerQuitEvent event) {
        this.manager.hideFromTops(event.getPlayer());
    }
}
