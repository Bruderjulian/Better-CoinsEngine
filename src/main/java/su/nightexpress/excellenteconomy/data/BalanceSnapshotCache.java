package su.nightexpress.excellenteconomy.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import su.nightexpress.excellenteconomy.api.currency.Currency;

/**
 * Lightweight, thread-safe snapshot cache of user balances per currency.
 * - Reads are lock-free and constant-time.
 * - Writers update after successful operations or external sync (Redis).
 * - Intended to serve Vault/other API calls on the main thread without DB hits.
 */
public class BalanceSnapshotCache {

    // userId -> (currencyId -> balance)
    private final Map<UUID, Map<String, Double>> balances = new ConcurrentHashMap<>();

    public double getBalance(UUID userId, String currencyId) {
        Map<String, Double> map = balances.get(userId);
        if (map == null)
            return 0D;
        return map.getOrDefault(currencyId, 0D);
    }

    public void setBalance(UUID userId, String currencyId, double value) {
        balances.computeIfAbsent(userId, id -> new ConcurrentHashMap<>()).put(currencyId, value);
    }

    public void setBalances(UUID userId, Map<String, Double> newBalances) {
        balances.compute(userId, (id, old) -> {
            if (old == null)
                return new ConcurrentHashMap<>(newBalances);
            old.clear();
            old.putAll(newBalances);
            return old;
        });
    }

    public void updateFromUser(UUID userId, Iterable<Currency> currencies,
            java.util.function.Function<Currency, Double> balanceProvider) {
        Map<String, Double> map = balances.computeIfAbsent(userId, id -> new ConcurrentHashMap<>());
        for (Currency c : currencies) {
            map.put(c.getId(), balanceProvider.apply(c));
        }
    }
}
