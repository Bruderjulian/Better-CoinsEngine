package su.nightexpress.excellenteconomy.tops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.Placeholders;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.config.Config;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.tops.menu.TopMenu;
import su.nightexpress.excellenteconomy.user.CoinsUser;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.NumberUtil;

public class TopManager extends AbstractManager<ExcellentEconomyPlugin> {

    private final CurrencyRegistry currencyRegistry;

    private final Map<String, Map<String, TopEntry>> topEntries;

    private TopMenu topMenu;

    public TopManager(final ExcellentEconomyPlugin plugin, final CurrencyRegistry currencyRegistry) {
        super(plugin);
        this.currencyRegistry = currencyRegistry;
        this.topEntries = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        if (Config.TOPS_USE_GUI.get()) {
            this.topMenu = this.addMenu(new TopMenu(this.plugin, this), "/menu/", "leaderboard.yml");
        }
        this.addListener(new TopsListener(this.plugin, this));
        this.addAsyncTask(this::updateBalances, Config.TOPS_UPDATE_INTERVAL.get());
    }

    @Override
    protected void onShutdown() {
        if (this.topMenu != null) {
            this.topMenu.clear();
        }
        this.topEntries.clear();
    }

    public void updateBalances() {
        this.topEntries.clear();

        final List<CoinsUser> users = this.plugin.getDataHandler().getUsers();

        users.removeIf(user -> {
            final Player player = user.getPlayer();
            if (player != null) {
                this.hideFromTops(player);
            }
            return user.isHiddenFromTops();
        });

        this.currencyRegistry.getCurrencies().forEach(currency -> {
            final AtomicInteger counter = new AtomicInteger(0);
            final Map<String, TopEntry> entries = new LinkedHashMap<>();

            users.stream()
                    .sorted(Comparator.comparingDouble((final CoinsUser user) -> user.getBalance(currency)).reversed())
                    .forEach(user -> {
                        entries.put(LowerCase.INTERNAL.apply(user.getName()), new TopEntry(counter.incrementAndGet(),
                                user.getName(), user.getId(), user.getBalance(currency)));
                    });

            this.topEntries.put(currency.getId(), entries);
        });
    }

    public void hideFromTops(final Player player) {
        this.plugin.getFoliaScheduler().runAsync(() -> {
            final CoinsUser user = this.plugin.getUserManager().getOrFetch(player);
            user.setHiddenFromTops(player.hasPermission(Perms.HIDE_FROM_TOPS));
        });
    }

    public boolean showLeaderboard(final CommandSender sender, final Currency currency,
            final int page) {
        if (sender instanceof final Player player && this.topMenu != null) {
            this.topMenu.open(player, currency);
            return true;
        }

        final int perPage = Config.TOPS_ENTRIES_PER_PAGE.get();

        final List<TopEntry> full = this.getTopEntries(currency);

        final List<List<TopEntry>> split = Lists.split(full, perPage);
        final int pages = split.size();
        final int index = Math.max(0, Math.min(pages, page) - 1);
        final int realPage = index + 1;

        final List<TopEntry> entries = pages > 0 ? split.get(index) : new ArrayList<>();

        final boolean hasNextPage = realPage < pages;
        final boolean hasPrevPage = index > 0;

        currency.sendPrefixed(Lang.TOP_LIST, sender, replacer -> replacer
                .replace(Placeholders.GENERIC_NEXT_PAGE,
                        () -> (hasNextPage ? Lang.TOP_LIST_NEXT_PAGE_ACTIVE : Lang.TOP_LIST_NEXT_PAGE_INACTIVE).text()
                                .replace(Placeholders.GENERIC_VALUE, String.valueOf(realPage + 1)))
                .replace(Placeholders.GENERIC_PREVIOUS_PAGE,
                        () -> (hasPrevPage ? Lang.TOP_LIST_PREVIOUS_PAGE_ACTIVE : Lang.TOP_LIST_PREVIOUS_PAGE_INACTIVE)
                                .text()
                                .replace(Placeholders.GENERIC_VALUE, String.valueOf(realPage - 1)))
                .replace(currency.replacePlaceholders())
                .replace(Placeholders.GENERIC_CURRENT, realPage)
                .replace(Placeholders.GENERIC_MAX, pages)
                .replace(Placeholders.GENERIC_ENTRY, list -> {
                    for (final TopEntry entry : entries) {
                        list.add(Lang.TOP_ENTRY.text()
                                .replace(Placeholders.GENERIC_POS, NumberUtil.format(entry.getPosition()))
                                .replace(Placeholders.GENERIC_BALANCE, currency.format(entry.getBalance()))
                                .replace(Placeholders.PLAYER_NAME, entry.getName()));
                    }
                }));

        return true;
    }

    public Map<String, Map<String, TopEntry>> getTopEntriesMap() {
        return this.topEntries;
    }

    public List<TopEntry> getTopEntries(final Currency currency) {
        return new ArrayList<>(this.topEntries.getOrDefault(currency.getId(), Collections.emptyMap()).values());
    }

    public TopEntry getTopEntry(final Currency currency, final String name) {
        return this.topEntries.getOrDefault(currency.getId(), Collections.emptyMap())
                .get(LowerCase.INTERNAL.apply(name));
    }

    public double getTotalBalance(final Currency currency) {
        return this.getTopEntries(currency).stream().filter(value -> value != null).mapToDouble(TopEntry::getBalance)
                .sum();
    }

    public void applyExternalTopEntries(final String currencyId,
            final Map<String, TopEntry> entries) {
        this.topEntries.put(currencyId, entries);
    }
}
