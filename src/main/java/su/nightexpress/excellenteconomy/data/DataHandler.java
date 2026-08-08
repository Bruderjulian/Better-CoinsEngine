package su.nightexpress.excellenteconomy.data;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.data.impl.CoinsUser;
import su.nightexpress.excellenteconomy.data.impl.CurrencySettings;
import su.nightexpress.excellenteconomy.data.serialize.CurrencySettingsSerializer;
import su.nightexpress.nightcore.db.AbstractUserDataManager;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.query.impl.UpdateQuery;
import su.nightexpress.nightcore.db.sql.query.type.ValuedQuery;
import su.nightexpress.nightcore.util.Lists;

public class DataHandler extends AbstractUserDataManager<ExcellentEconomyPlugin, CoinsUser> {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(CurrencySettings.class, new CurrencySettingsSerializer())
            .create();

    static final Column COLUMN_SETTINGS = Column.of("settings", ColumnType.STRING);
    static final Column COLUMN_HIDE_FROM_TOPS = Column.of("hiddenFromTops", ColumnType.BOOLEAN);

    static final Map<String, Column> CURRENCY_COLUMNS = new HashMap<>();

    private boolean synchronizationActive; // A little helper to pause synchronization during operations disable

    public DataHandler(ExcellentEconomyPlugin plugin) {
        super(plugin);
        this.setSynchronizationActive(true);
    }

    public void setSynchronizationActive(boolean synchronizationActive) {
        this.synchronizationActive = synchronizationActive;
    }

    public String getUsersTable() {
        return this.tableUsers;
    }

    @Override
    protected void onClose() {
        super.onClose();
        CURRENCY_COLUMNS.clear();
    }

    @Override

    protected Function<ResultSet, CoinsUser> createUserFunction() {
        return DataQueries.USER_LOADER;
    }

    @Override

    protected GsonBuilder registerAdapters(GsonBuilder builder) {
        return builder;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.dropColumn(this.tableUsers, "balances", "currencyData");
        this.addColumn(this.tableUsers, COLUMN_SETTINGS, "{}");
        this.addColumn(this.tableUsers, COLUMN_HIDE_FROM_TOPS, String.valueOf(0));
    }

    public static Column getCurrencyColumn(Currency currency) {
        return getCurrencyColumn(currency.getId());
    }

    public static Column getCurrencyColumn(String currencyId) {
        return CURRENCY_COLUMNS.get(currencyId);
    }

    public static boolean isCurrencyColumnCached(Currency currency) {
        return CURRENCY_COLUMNS.containsKey(currency.getId());
    }

    public void onCurrencyRegister(Currency currency) {
        this.addCurrencyColumn(currency);
    }

    public void onCurrencyUnload(Currency currency) {
        CURRENCY_COLUMNS.remove(currency.getId());
    }

    public void addCurrencyColumn(Currency currency) {
        Column column = Column.of(currency.getColumnName(), ColumnType.DOUBLE);
        this.addColumn(this.tableUsers, column, String.valueOf(currency.getStartValue()));
        CURRENCY_COLUMNS.put(currency.getId(), column);
    }

    @Override
    protected void addUpsertQueryData(ValuedQuery<?, CoinsUser> query) {
        query.setValue(COLUMN_SETTINGS, user -> GSON.toJson(user.getSettingsMap()));
        query.setValue(COLUMN_HIDE_FROM_TOPS, user -> String.valueOf(user.isHiddenFromTops() ? 1 : 0));

        CURRENCY_COLUMNS.forEach((id, column) -> {
            query.setValue(column, user -> String.valueOf(user.getBalance().get(id)));
        });
    }

    @Override
    protected void addSelectQueryData(SelectQuery<CoinsUser> query) {
        query.column(COLUMN_SETTINGS);
        query.column(COLUMN_HIDE_FROM_TOPS);
        CURRENCY_COLUMNS.values().forEach(query::column);
    }

    @Override
    protected void addTableColumns(List<Column> columns) {
        columns.add(COLUMN_SETTINGS);
        columns.add(COLUMN_HIDE_FROM_TOPS);
    }

    @Override
    public void onSynchronize() {
        // Do not synchronize data if operations are disabled to prevent data
        // loss/clash.
        if (!this.synchronizationActive)
            return;

        this.synchronizer.syncAll();
    }

    public void resetBalances(Currency currency) {
        this.resetBalances(Lists.newSet(currency));
    }

    public void resetBalances(Collection<Currency> currencies) {
        UpdateQuery<Object> query = new UpdateQuery<>();

        for (Currency currency : currencies) {
            query.setValue(getCurrencyColumn(currency), o -> String.valueOf(currency.getStartValue()));
        }

        this.update(this.tableUsers, query, new Object()); // Little hack to bypass query params.
    }
}
