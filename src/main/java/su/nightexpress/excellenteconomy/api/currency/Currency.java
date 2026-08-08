package su.nightexpress.excellenteconomy.api.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.number.CompactNumber;
import su.nightexpress.nightcore.util.placeholder.Replacer;

public interface Currency {

    void onRegister();

    void onUnregister();

    UnaryOperator<String> replacePlaceholders();

    void sendPrefixed(MessageLocale locale, CommandSender sender);

    void sendPrefixed(MessageLocale locale, CommandSender sender, Consumer<Replacer> consumer);

    boolean hasPermission(Player player);

    boolean isPrimary();

    boolean isUnlimited();

    boolean isLimited();

    boolean isInteger();

    boolean isUnderLimit(double value);

    default boolean isUnderLimit(BigDecimal value) {
        return this.isUnderLimit(value.doubleValue());
    }

    @Deprecated
    default double fine(double amount) {
        return this.floorIfNeeded(amount);
    }

    double floorIfNeeded(double amount);

    default BigDecimal floorIfNeeded(BigDecimal amount) {
        BigDecimal nonNegative = amount.max(BigDecimal.ZERO);
        if (!this.isDecimal()) {
            return nonNegative.setScale(0, RoundingMode.FLOOR);
        }
        return nonNegative;
    }

    @Deprecated
    default double limit(double amount) {
        return this.limitIfNeeded(amount);
    }

    double limitIfNeeded(double amount);

    default BigDecimal limitIfNeeded(BigDecimal amount) {
        if (this.isLimited()) {
            double max = this.getMaxValue();
            return amount.min(BigDecimal.valueOf(max));
        }
        return amount;
    }

    @Deprecated
    default double fineAndLimit(double amount) {
        return this.floorAndLimit(amount);
    }

    double floorAndLimit(double amount);

    default BigDecimal floorAndLimit(BigDecimal amount) {
        return this.floorIfNeeded(this.limitIfNeeded(amount));
    }

    String getPermission();

    String formatValue(double balance);

    default String formatValue(BigDecimal balance) {
        return this.formatValue(balance.doubleValue());
    }

    String format(double balance);

    default String format(BigDecimal balance) {
        return this.format(balance.doubleValue());
    }

    @Deprecated
    default CompactNumber formatCompactValue(double balance) {
        return this.compacted(balance);
    }

    CompactNumber compacted(double balance);

    default CompactNumber compacted(BigDecimal balance) {
        return this.compacted(balance.doubleValue());
    }

    String formatCompact(double balance);

    default String formatCompact(BigDecimal balance) {
        return this.formatCompact(balance.doubleValue());
    }

    String getId();

    String getName();

    void setName(String name);

    String getPrefix();

    void setPrefix(String prefix);

    String getSymbol();

    void setSymbol(String symbol);

    String getFormat();

    void setFormat(String format);

    String getFormatShort();

    void setFormatShort(String formatShort);

    String[] getCommandAliases();

    void setCommandAliases(String... commandAliases);

    String getColumnName();

    void setColumnName(String dataColumn);

    @Deprecated
    ItemStack getIcon();

    @Deprecated
    void setIcon(ItemStack icon);

    NightItem icon();

    void setIcon(NightItem icon);

    boolean isDecimal();

    void setDecimal(boolean decimal);

    boolean isPermissionRequired();

    void setPermissionRequired(boolean permissionRequired);

    boolean isSynchronizable();

    void setSynchronizable(boolean dataSync);

    boolean isTransferAllowed();

    void setTransferAllowed(boolean transferAllowed);

    double getMinTransferAmount();

    void setMinTransferAmount(double minTransferAmount);

    double getStartValue();

    void setStartValue(double startValue);

    double getMaxValue();

    void setMaxValue(double maxValue);

    @Deprecated
    boolean isVaultEconomy();

    boolean isExchangeAllowed();

    void setExchangeAllowed(boolean exchangeAllowed);

    Map<String, Double> getExchangeRates();

    double getExchangeRate(Currency currency);

    double getExchangeRate(String id);

    boolean canExchangeTo(Currency other);

    double getExchangeResult(Currency other, double amount);

    default BigDecimal getExchangeResult(Currency other, BigDecimal amount) {
        double result = this.getExchangeResult(other, amount.doubleValue());
        return BigDecimal.valueOf(result);
    }

    boolean isLeaderboardEnabled();
}
