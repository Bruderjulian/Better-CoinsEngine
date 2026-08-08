package su.nightexpress.excellenteconomy.api;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.bukkit.entity.Player;

import su.nightexpress.excellenteconomy.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandManager;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.currency.operation.OperationResult;
import su.nightexpress.excellenteconomy.data.impl.CoinsUser;
import su.nightexpress.excellenteconomy.user.UserManager;

public class CoinsEngineAPI {

    private static CoinsEnginePlugin plugin;

    public static void load(CoinsEnginePlugin plugin) {
        CoinsEngineAPI.plugin = plugin;
    }

    public static void clear() {
        plugin = null;
    }

    public static boolean isLoaded() {
        return plugin != null;
    }

    public static CoinsEnginePlugin plugin() {
        if (plugin == null)
            throw new IllegalStateException("API is not yet initialized!");

        return plugin;
    }

    public static UserManager getUserManager() {
        return plugin().getUserManager();
    }

    public static CurrencyManager getCurrencyManager() {
        return plugin().getCurrencyManager();
    }

    public static CurrencyRegistry getCurrencyRegistry() {
        return plugin().getCurrencyRegistry();
    }

    public static CommandManager getCommandManager() {
        return plugin().getCommander();
    }

    public static Collection<Currency> getCurrencies() { // keep it Collection for the API compatibility
        return getCurrencyRegistry().getCurrencies();
    }

    public static Currency getCurrency(String id) {
        return getCurrencyRegistry().getById(id);
    }

    public static boolean hasCurrency(String id) {
        return getCurrencyRegistry().isRegistered(id);
    }

    public static void regsiterCurrency(Currency currency) {
        getCurrencyManager().registerCurrency(currency);
    }

    public static void regsiterCurrencyWithCommands(Currency currency) {
        regsiterCurrency(currency);
        getCommandManager().getCurrencyCommands().loadCommands(currency);
    }

    public static void unregsiterCurrency(Currency currency) {
        getCommandManager().getCurrencyCommands().unregisterCommands(currency);
        getCurrencyManager().unregisterCurrency(currency);
    }

    public static double getBalance(UUID playerId, String currencyName) {
        Currency currency = getCurrency(currencyName);

        return currency == null ? 0D : getBalance(playerId, currency);
    }

    public static double getBalance(UUID playerId, Currency currency) {
        CoinsUser user = getUserData(playerId);

        return user == null ? 0D : user.getBalance(currency);
    }

    public static double getBalance(Player player, Currency currency) {
        return getUserData(player).getBalance(currency);
    }

    public static boolean addBalance(UUID playerId, String currencyName, double amount) {
        Currency currency = getCurrency(currencyName);
        return currency != null && addBalance(playerId, currency, amount);
    }

    public static boolean addBalance(UUID playerId, Currency currency, double amount) {
        return addBalance(playerId, currency, amount, operationContext());
    }

    public static boolean addBalance(UUID playerId, Currency currency, double amount, OperationContext context) {
        return editBalance(playerId, user -> getCurrencyManager().give(context, user, currency, amount));
    }

    public static void addBalance(Player player, Currency currency, double amount) {
        addBalance(player, currency, amount, operationContext());
    }

    public static boolean addBalance(Player player, Currency currency, double amount, OperationContext context) {
        return getCurrencyManager().give(context, player, currency, amount) == OperationResult.SUCCESS;
    }

    public static boolean removeBalance(UUID playerId, String currencyName, double amount) {
        Currency currency = getCurrency(currencyName);
        return currency != null && removeBalance(playerId, currency, amount);
    }

    public static boolean removeBalance(UUID playerId, Currency currency, double amount) {
        return removeBalance(playerId, currency, amount, operationContext());
    }

    public static void removeBalance(Player player, Currency currency, double amount) {
        removeBalance(player, currency, amount, operationContext());
    }

    public static boolean removeBalance(UUID playerId, Currency currency, double amount, OperationContext context) {
        return editBalance(playerId, user -> getCurrencyManager().remove(context, user, currency, amount));
    }

    public static boolean removeBalance(Player player, Currency currency, double amount, OperationContext context) {
        return getCurrencyManager().remove(context, player, currency, amount) == OperationResult.SUCCESS;
    }

    public static boolean setBalance(UUID playerId, String currencyName, double amount) {
        Currency currency = getCurrency(currencyName);
        return currency != null && setBalance(playerId, currency, amount);
    }

    public static boolean setBalance(UUID playerId, Currency currency, double amount) {
        return setBalance(playerId, currency, amount, operationContext());
    }

    public static void setBalance(Player player, Currency currency, double amount) {
        setBalance(player, currency, amount, operationContext());
    }

    public static boolean setBalance(UUID playerId, Currency currency, double amount, OperationContext context) {
        return editBalance(playerId, user -> getCurrencyManager().set(context, user, currency, amount));
    }

    public static boolean setBalance(Player player, Currency currency, double amount, OperationContext context) {
        return getCurrencyManager().set(context, player, currency, amount) == OperationResult.SUCCESS;
    }

    private static boolean editBalance(UUID id, Function<CoinsUser, OperationResult> function) {
        CoinsUser user = getUserData(id);
        if (user == null)
            return false;

        OperationResult result = function.apply(user);
        return result == OperationResult.SUCCESS;
    }

    private static OperationContext operationContext() {
        return OperationContext.custom("API").silentFor(NotificationTarget.USER, NotificationTarget.EXECUTOR,
                NotificationTarget.CONSOLE_LOGGER);
    }

    public static CoinsUser getUserData(Player player) {
        return getUserManager().getOrFetch(player);
    }

    public static CoinsUser getUserData(String name) {
        return getUserManager().getOrFetch(name);
    }

    public static CoinsUser getUserData(UUID uuid) {
        return getUserManager().getOrFetch(uuid);
    }

    public static CompletableFuture<CoinsUser> getUserDataAsync(String name) {
        return getUserManager().getUserDataAsync(name);
    }

    public static CompletableFuture<CoinsUser> getUserDataAsync(UUID uuid) {
        return getUserManager().getUserDataAsync(uuid);
    }
}
