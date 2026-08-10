package su.nightexpress.excellenteconomy.api.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.user.CoinsUser;

public final class ChangeBalanceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CoinsUser user;
    private final Currency currency;
    private final double oldAmount;
    private final double newAmount;

    private boolean cancelled;

    public ChangeBalanceEvent(CoinsUser user, Currency currency, double oldAmount, double newAmount) {
        super(!Bukkit.isPrimaryThread());
        this.user = user;
        this.currency = currency;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CoinsUser getUser() {
        return this.user;
    }

    public Player getPlayer() {
        return this.user.getPlayer();
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public double getOldAmount() {
        return this.oldAmount;
    }

    public double getNewAmount() {
        return this.newAmount;
    }
}
