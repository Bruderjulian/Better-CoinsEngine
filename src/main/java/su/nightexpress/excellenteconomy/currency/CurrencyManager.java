package su.nightexpress.excellenteconomy.currency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import su.nightexpress.excellenteconomy.COEFiles;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.Placeholders;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.config.Config;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.currency.impl.AbstractCurrency;
import su.nightexpress.excellenteconomy.currency.impl.NormalCurrency;
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.currency.operation.OperationExecutor;
import su.nightexpress.excellenteconomy.currency.operation.OperationResult;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.data.impl.CoinsUser;
import su.nightexpress.excellenteconomy.data.impl.CurrencySettings;
import su.nightexpress.excellenteconomy.hook.HookPlugin;
import su.nightexpress.excellenteconomy.user.UserManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.db.AbstractUser;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Strings;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;

public class CurrencyManager extends AbstractManager<ExcellentEconomyPlugin> {

    private final CurrencyRegistry registry;
    private final DataHandler dataHandler;
    private final UserManager userManager;

    private boolean operationsAllowed;
    private CurrencyLogger logger;

    public CurrencyManager(final ExcellentEconomyPlugin plugin, final CurrencyRegistry registry,
            final DataHandler dataHandler,
            final UserManager userManager) {
        super(plugin);
        this.registry = registry;
        this.dataHandler = dataHandler;
        this.userManager = userManager;
        this.allowOperations();
    }

    @Override
    protected void onLoad() {
        this.createDefaults();
        this.migrateSettings();
        FileUtil.getConfigFiles(this.getDirectory()).stream().map(java.io.File::toPath).forEach(this::loadCurrency);

        try {
            this.loadLogger();
        } catch (IOException | IllegalArgumentException exception) {
            this.plugin.error("Could not create operations logger: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @Override
    protected void onShutdown() {
        this.registry.getCurrencies().forEach(this::unregisterCurrency);

        if (this.logger != null)
            this.logger.shutdown();
        this.disableOperations();
    }

    private void migrateSettings() {
        FileUtil.getConfigFiles(this.getDirectory()).forEach(file -> {
            final String fileName = file.getName();
            if (!fileName.endsWith(FileConfig.EXTENSION))
                return;

            final FileConfig config = FileConfig.load(file.toPath());
            if (!config.contains("Economy"))
                return;

            if (config.getBoolean("Economy.Vault")) {
                final String name = fileName.substring(0, fileName.length() - FileConfig.EXTENSION.length());
                Config.INTEGRATION_VAULT_ECONOMY_CURRENCY.set(name);
                Config.INTEGRATION_VAULT_ECONOMY_CURRENCY.write(this.plugin.getConfig());
            }

            config.remove("Economy");
            config.saveChanges();
        });
    }

    private void loadCurrency(final Path path) throws IllegalStateException {
        final String fileName = path.getFileName().toString();
        if (!fileName.endsWith(FileConfig.EXTENSION))
            return;

        final String name = fileName.substring(0, fileName.length() - FileConfig.EXTENSION.length());
        final String id = Strings.varStyle(name)
                .orElseThrow(() -> new IllegalStateException("Malformed file name '" + fileName + "'"));

        final boolean isVault = Plugins.isInstalled(HookPlugin.VAULT) && Config.INTEGRATION_VAULT_ENABLED.get();
        final boolean isGoodId = Config.INTEGRATION_VAULT_ECONOMY_CURRENCY.get().equalsIgnoreCase(id);

        AbstractCurrency currency;

        if (isVault && isGoodId) {
            currency = CurrencyFactory.createEconomy(path, id, this.plugin, this, this.dataHandler, this.userManager);
        } else {
            currency = CurrencyFactory.createNormal(path, id);
        }

        // Currently useless, but will be useful once we remake the plugin reload
        // system.
        if (currency.isPrimary() && this.registry.hasPrimary()) {
            this.plugin.warn("Could not load primary currency '" + currency.getId()
                    + "' as there is already one present. Reboot the server if you want to change your primary currency.");
            return;
        }

        currency.load();

        this.registerCurrency(currency);
    }

    private void createDefaults() {
        final File dir = new File(this.getDirectory());
        if (dir.exists())
            return;

        this.createCurrency("coins", currency -> {
            currency.setSymbol("⛂");
            currency.setIcon(NightItem.fromType(Material.SUNFLOWER));
            currency.setDecimal(false);
        });

        this.createCurrency("money", currency -> {
            currency.setSymbol("$");
            currency.setFormat(Placeholders.CURRENCY_SYMBOL + Placeholders.GENERIC_AMOUNT);
            currency.setFormat(currency.getFormat());
            currency.setIcon(NightItem.fromType(Material.GOLD_NUGGET));
            currency.setDecimal(true);
        });
    }

    private void loadLogger() throws IOException, IllegalArgumentException {
        final boolean logToConsole = Config.LOGS_TO_CONSOLE.get();
        final boolean logToFile = Config.LOGS_TO_FILE.get();
        if (!logToConsole && !logToFile)
            return;

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.LOGS_DATE_FORMAT.get());
        final Path filePath = Paths.get(this.plugin.getDataFolder().getAbsolutePath(), COEFiles.FILE_OPERATIONS);

        this.logger = new CurrencyLogger(this.plugin, formatter, filePath, logToConsole, logToFile);
        this.addAsyncTask(() -> this.logger.write(), Config.LOGS_WRITE_INTERVAL.get());
    }

    public String getDirectory() {
        return this.plugin.getDataFolder() + COEFiles.DIR_CURRENCIES;
    }

    public void registerCurrency(final Currency currency) {
        if (this.registry.isRegistered(currency.getId())) {
            this.plugin.error("Could not register duplicated currency: '" + currency.getId() + "'!");
            return;
        }

        if (DataHandler.isCurrencyColumnCached(currency)) {
            this.plugin.error("Currency '" + currency.getId() + "' tried to use column '" + currency.getColumnName()
                    + "' which is already used by other currency!");
            return;
        }

        this.registry.add(currency);
        this.dataHandler.onCurrencyRegister(currency);
        this.plugin.info("Currency registered: '" + currency.getId() + "'.");
    }

    public boolean unregisterCurrency(final Currency currency) {
        return this.unregisterCurrency(currency.getId());
    }

    public boolean unregisterCurrency(final String id) {
        final Currency currency = this.registry.remove(id);
        if (currency == null)
            return false;

        this.dataHandler.onCurrencyUnload(currency);
        this.plugin.info("Currency unregistered: '" + currency.getId() + "'.");
        return true;
    }

    public Collection<Currency> getCurrencies() {
        return this.registry.getCurrencies();
    }

    public void allowOperations() {
        this.operationsAllowed = true;
        this.dataHandler.setSynchronizationActive(true);
    }

    public void disableOperations() {
        this.operationsAllowed = false;
        this.dataHandler.setSynchronizationActive(false);
    }

    public boolean canPerformOperations() {
        return this.operationsAllowed;
    }

    private boolean assertOperationsEnabled(final OperationContext context) {
        if (!this.canPerformOperations()) {
            context.getBukkitSender().ifPresent(sender -> Lang.CURRENCY_OPERATION_DISABLED.message().send(sender));
            return false;
        }
        return true;
    }

    public NormalCurrency createCurrency(final String id, final Consumer<NormalCurrency> consumer) {
        final Path path = Paths.get(this.getDirectory(), FileConfig.withExtension(id));
        final NormalCurrency currency = new NormalCurrency(path, id);

        consumer.accept(currency);
        currency.write();
        return currency;
    }

    public boolean createCurrency(final CommandSender sender, final String name, final String symbol,
            final boolean decimals) {
        final String id = Strings.varStyle(name).orElse(null);
        if (id == null) {
            Lang.CURRENCY_CREATE_BAD_NAME.message().send(sender);
            return false;
        }

        if (this.registry.isRegistered(id)) {
            Lang.CURRENCY_CREATE_DUPLICATED.message().send(sender);
            return false;
        }

        final NormalCurrency created = this.createCurrency(id, currency -> {
            currency.setSymbol(symbol);
            currency.setDecimal(decimals);
        });

        created.updateMessagePrefix();

        this.registerCurrency(created);
        this.plugin.getCommander().getCurrencyCommands().loadCommands(created);

        Lang.CURRENCY_CREATE_SUCCESS.message().send(sender,
                replacer -> replacer.replace(created.replacePlaceholders()));
        return true;
    }

    public void showBalance(final CommandSender sender, final Currency currency) {
        this.showBalance(sender, sender.getName(), currency);
    }

    public void showBalance(final CommandSender sender, final String name, final Currency currency) {
        final boolean isOwn = sender.getName().equalsIgnoreCase(name);

        this.userManager.manageUser(name, user -> {
            if (user == null) {
                CoreLang.ERROR_INVALID_PLAYER.withPrefix(this.plugin).send(sender);
                return;
            }

            currency.sendPrefixed((isOwn ? Lang.CURRENCY_BALANCE_DISPLAY_OWN : Lang.CURRENCY_BALANCE_DISPLAY_OTHERS),
                    sender, replacer -> replacer
                            .replace(Placeholders.PLAYER_NAME, user.getName())
                            .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
        });
    }

    public void showWallet(final Player player) {
        this.showWallet(player, player.getName());
    }

    public void showWallet(final CommandSender sender, final String name) {
        final boolean isOwn = sender.getName().equalsIgnoreCase(name);

        this.userManager.manageUser(name, user -> {
            if (user == null) {
                CoreLang.ERROR_INVALID_PLAYER.withPrefix(this.plugin).send(sender);
                return;
            }

            (isOwn ? Lang.CURRENCY_WALLET_OWN : Lang.CURRENCY_WALLET_OTHERS).message().send(sender, replacer -> replacer
                    .replace(Placeholders.GENERIC_ENTRY, list -> {
                        this.registry.getCurrencies().stream().sorted(Comparator.comparing(Currency::getId))
                                .forEach(currency -> {
                                    if (sender instanceof final Player player && !currency.hasPermission(player))
                                        return;

                                    list.add(Replacer.create()
                                            .replace(currency.replacePlaceholders())
                                            .replace(Placeholders.GENERIC_BALANCE,
                                                    currency.format(user.getBalance(currency)))
                                            .apply(Lang.CURRENCY_WALLET_ENTRY.text()));
                                });
                    })
                    .replace(Placeholders.PLAYER_NAME, user.getName()));
        });
    }

    public void togglePayments(final Player player, final Currency currency) {
        this.togglePayments(player, player.getName(), currency, false);
    }

    public void togglePayments(final CommandSender sender, final String name, final Currency currency,
            final boolean silent) {
        final boolean isOwn = sender.getName().equalsIgnoreCase(name);

        this.userManager.manageUser(name, user -> {
            if (user == null) {
                CoreLang.ERROR_INVALID_PLAYER.withPrefix(this.plugin).send(sender);
                return;
            }

            final CurrencySettings settings = user.getSettings(currency);
            settings.setPaymentsEnabled(!settings.isPaymentsEnabled());
            this.userManager.save(user);

            if (!isOwn) {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_PAYMENTS_TARGET, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, user.getName())
                        .replace(Placeholders.GENERIC_STATE,
                                CoreLang.STATE_ENABLED_DISALBED.get(settings.isPaymentsEnabled())));
            }

            final Player target = user.getPlayer();
            if (!silent && target != null) {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_PAYMENTS_TOGGLE, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_STATE,
                                CoreLang.STATE_ENABLED_DISALBED.get(settings.isPaymentsEnabled())));
            }
        });
    }

    public OperationResult give(final OperationContext context, final Player player, final Currency currency,
            final double amount) {
        return this.give(context, this.userManager.getOrFetch(player), currency, amount);
    }

    public OperationResult give(final OperationContext context, final CoinsUser user, final Currency currency,
            final double amount) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();

        user.addBalance(currency, amount);
        this.userManager.save(user);
        // Custom: publish Redis sync
        this.plugin.getRedisSyncManager().ifPresent(sync -> {
            sync.publishCurrencyOperation(user.getId(), currency.getId(), "give", amount, user.getBalance(currency));
            sync.publishUserBalance(user);
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s gave %s to %s. New balance: %s"
                    .formatted(currency.getId(), executor.getName(), currency.format(amount), user.getName(),
                            currency.format(user.getBalance(currency))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_GIVE_DONE, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, user::getName)
                        .replace(Placeholders.GENERIC_AMOUNT, () -> currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(user.getBalance(currency))));
            });
        }

        if (context.shouldNotify(NotificationTarget.USER)) {
            final Player target = user.getPlayer();
            if (target != null) {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_GIVE_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, () -> currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(user.getBalance(currency))));
            }
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult giveAll(final OperationContext context, final Currency currency, final double amount) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();
        final Set<CoinsUser> users = this.userManager.getLoaded();

        users.forEach(user -> {
            final Player target = user.getPlayer();
            if (target == null)
                return; // Only online players should be affected.

            user.addBalance(currency, amount);
            this.userManager.save(user);

            if (context.shouldNotify(NotificationTarget.USER)) {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_GIVE_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, () -> currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(user.getBalance(currency))));
            }
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s gave %s to all online players. Affected players (%s): %s"
                    .formatted(currency.getId(), executor.getName(), currency.format(amount), users.size(),
                            users.stream().map(AbstractUser::getName).collect(Collectors.joining(", "))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_GIVE_ALL_DONE, sender, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount)));
            });
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult removeAll(final OperationContext context, final Currency currency, final double amount) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();
        final Set<CoinsUser> users = this.userManager.getLoaded();

        users.forEach(user -> {
            final Player target = user.getPlayer();
            if (target == null)
                return; // Only online players should be affected.

            user.removeBalance(currency, amount);
            this.userManager.save(user);

            if (context.shouldNotify(NotificationTarget.USER)) {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_REMOVE_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, () -> currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(user.getBalance(currency))));
            }
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s removed %s from all online players. Affected players (%s): %s"
                    .formatted(currency.getId(), executor.getName(), currency.format(amount), users.size(),
                            users.stream().map(AbstractUser::getName).collect(Collectors.joining(", "))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_REMOVE_ALL_DONE, sender, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount)));
            });
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult remove(final OperationContext context, final Player player, final Currency currency,
            final double amount) {
        return this.remove(context, this.userManager.getOrFetch(player), currency, amount);
    }

    public OperationResult remove(final OperationContext context, final CoinsUser user, final Currency currency,
            final double amount) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();

        user.removeBalance(currency, amount);
        this.userManager.save(user);
        // Custom: publish Redis sync
        this.plugin.getRedisSyncManager().ifPresent(sync -> {
            sync.publishCurrencyOperation(user.getId(), currency.getId(), "remove", amount, user.getBalance(currency));
            sync.publishUserBalance(user);
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s took %s from %s's balance. New balance: %s"
                    .formatted(currency.getId(), executor.getName(), currency.format(amount), user.getName(),
                            currency.format(user.getBalance(currency))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_REMOVE_DONE, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, user.getName())
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        if (context.shouldNotify(NotificationTarget.USER)) {
            Optional.ofNullable(user.getPlayer()).ifPresent(target -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_REMOVE_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult set(final OperationContext context, final Player player, final Currency currency,
            final double amount) {
        return this.set(context, this.userManager.getOrFetch(player), currency, amount);
    }

    public OperationResult set(final OperationContext context, final CoinsUser user, final Currency currency,
            final double amount) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();

        user.setBalance(currency, amount);
        this.userManager.save(user);
        // Custom: publish Redis sync
        this.plugin.getRedisSyncManager().ifPresent(sync -> {
            sync.publishCurrencyOperation(user.getId(), currency.getId(), "set", amount, user.getBalance(currency));
            sync.publishUserBalance(user);
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s set %s's balance to %s. New balance: %s"
                    .formatted(currency.getId(), executor.getName(), user.getName(), currency.format(amount),
                            currency.format(user.getBalance(currency))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_SET_DONE, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, user.getName())
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        if (context.shouldNotify(NotificationTarget.USER)) {
            Optional.ofNullable(user.getPlayer()).ifPresent(target -> {
                currency.sendPrefixed(Lang.COMMAND_CURRENCY_SET_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult reset(final OperationContext context, final Player player, final Currency currency) {
        return this.reset(context, this.userManager.getOrFetch(player), currency);
    }

    public OperationResult reset(final OperationContext context, final CoinsUser user, final Currency currency) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();

        user.resetBalance(currency);
        this.userManager.save(user);
        // Custom: publish Redis sync
        this.plugin.getRedisSyncManager().ifPresent(sync -> {
            sync.publishCurrencyOperation(user.getId(), currency.getId(), "reset", 0D, user.getBalance(currency));
            sync.publishUserBalance(user);
        });

        if (this.logger != null && context.shouldNotifyLogger()) {
            this.logger.addEntry(context, "[%s] %s reset %s's balance of %s to %s."
                    .formatted(currency.getId(), executor.getName(), user.getName(), currency.getName(),
                            currency.format(user.getBalance(currency))));
        }

        if (context.shouldNotify(NotificationTarget.EXECUTOR)) {
            executor.getBukkitSender().ifPresent(sender -> {
                currency.sendPrefixed(Lang.CURRENCY_OPERATION_RESET_FEEDBACK, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, user.getName())
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        if (context.shouldNotify(NotificationTarget.USER)) {
            Optional.ofNullable(user.getPlayer()).ifPresent(target -> {
                currency.sendPrefixed(Lang.CURRENCY_OPERATION_RESET_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency))));
            });
        }

        return OperationResult.SUCCESS;
    }

    public OperationResult resetAll(final OperationContext context, final CommandSender sender, final Currency currency,
            final boolean includeOffline) {
        if (!this.assertOperationsEnabled(context))
            return OperationResult.FAILURE;

        final OperationExecutor executor = context.getExecutor();
        final Set<CoinsUser> users = this.userManager.getLoaded();

        this.plugin.runTaskAsync(() -> {
            Lang.COMMAND_CURRENCY_RESET_ALL_STARTED.message().send(sender,
                    replacer -> replacer.replace(currency.replacePlaceholders()));

            this.disableOperations();
            this.dataHandler.resetBalances(currency);
            for (final CoinsUser user : users) {
                user.resetBalance(currency);
                this.userManager.save(user);
                if (context.shouldNotify(NotificationTarget.USER)) {
                    currency.sendPrefixed(Lang.CURRENCY_OPERATION_RESET_NOTIFY, sender, replacer -> replacer
                            .replace(Placeholders.GENERIC_BALANCE, () -> currency.format(user.getBalance(currency))));
                }
            }
            this.allowOperations();

            Lang.COMMAND_CURRENCY_RESET_ALL_COMPLETED.message().send(sender,
                    replacer -> replacer.replace(currency.replacePlaceholders()));
            if (this.logger != null && context.shouldNotifyLogger()) {
                this.logger.addEntry(context, "[%s] %s gave %s to all online players. Affected players (%s): %s"
                        .formatted(currency.getId(), executor.getName(), users.size(),
                                users.stream().map(AbstractUser::getName).collect(Collectors.joining(", "))));
            }
        });

        return OperationResult.SUCCESS;
    }

    public boolean send(final Player sender, final String targetName, final Currency currency, final double rawAmount) {
        final OperationContext context = OperationContext.of(sender);

        if (!this.assertOperationsEnabled(context))
            return false;

        final double amount = currency.floorIfNeeded(rawAmount);
        if (amount <= 0D)
            return false;

        if (sender.getName().equalsIgnoreCase(targetName)) {
            CoreLang.COMMAND_EXECUTION_NOT_YOURSELF.withPrefix(this.plugin).send(sender);
            return false;
        }

        final double minAmount = currency.getMinTransferAmount();
        if (minAmount > 0 && amount < minAmount) {
            currency.sendPrefixed(Lang.CURRENCY_SEND_ERROR_TOO_LOW, sender,
                    replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, currency.format(minAmount)));
            return false;
        }

        final CoinsUser fromUser = this.userManager.getOrFetch(sender);
        if (amount > fromUser.getBalance(currency)) {
            currency.sendPrefixed(Lang.CURRENCY_SEND_ERROR_NOT_ENOUGH, sender);
            return false;
        }

        this.userManager.manageUser(targetName, targetUser -> {
            if (targetUser == null) {
                CoreLang.ERROR_INVALID_PLAYER.withPrefix(this.plugin).send(sender);
                return;
            }

            final CurrencySettings settings = targetUser.getSettings(currency);
            if (!settings.isPaymentsEnabled()) {
                currency.sendPrefixed(Lang.CURRENCY_SEND_ERROR_NO_PAYMENTS, sender, replacer -> replacer
                        .replace(Placeholders.PLAYER_NAME, targetUser.getName()));
                return;
            }

            targetUser.addBalance(currency, amount);
            fromUser.removeBalance(currency, amount);

            this.userManager.save(targetUser);
            this.userManager.save(fromUser);

            // Custom: publish Redis sync + cross-server payment notify
            this.plugin.getRedisSyncManager().ifPresent(sync -> {
                sync.publishUserBalance(fromUser);
                sync.publishUserBalance(targetUser);
                sync.publishPaymentNotification(targetUser.getId(), sender.getName(), currency.getId(), amount,
                        targetUser.getBalance(currency));
            });

            currency.sendPrefixed(Lang.CURRENCY_SEND_DONE_SENDER, sender, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                    .replace(Placeholders.GENERIC_BALANCE, fromUser.getBalance(currency))
                    .replace(Placeholders.PLAYER_NAME, targetUser.getName()));

            Optional.ofNullable(targetUser.getPlayer()).ifPresent(target -> {
                currency.sendPrefixed(Lang.CURRENCY_SEND_DONE_NOTIFY, target, replacer -> replacer
                        .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                        .replace(Placeholders.GENERIC_BALANCE, targetUser.getBalance(currency))
                        .replace(Placeholders.PLAYER_NAME, sender.getName()));
            });

            this.logger.addEntry(context, "[%s] %s paid %s to %s. New balances: %s and %s.".formatted(
                    currency.getId(),
                    sender.getName(),
                    currency.format(amount),
                    targetUser.getName(),
                    currency.format(fromUser.getBalance(currency)),
                    currency.format(targetUser.getBalance(currency))));
        });

        return true;
    }

    public boolean exchange(final Player player, final Currency sourceCurrency, final Currency targetCurrency,
            final double initAmount) {
        final OperationContext context = OperationContext.of(player);

        if (!this.assertOperationsEnabled(context))
            return false;

        if (!sourceCurrency.isExchangeAllowed()) {
            sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_DISABLED, player);
            return false;
        }

        final double amount = sourceCurrency.floorIfNeeded(initAmount);
        if (amount <= 0D) {
            sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_LOW_AMOUNT, player);
            return false;
        }

        final CoinsUser user = this.userManager.getOrFetch(player);
        if (user.getBalance(sourceCurrency) < amount) {
            sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_LOW_BALANCE, player, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, sourceCurrency.format(amount)));
            return false;
        }

        if (!sourceCurrency.canExchangeTo(targetCurrency)) {
            sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_NO_RATE, player, replacer -> replacer
                    .replace(Placeholders.GENERIC_NAME, targetCurrency.getName()));
            return false;
        }

        final double result = sourceCurrency.getExchangeResult(targetCurrency, amount);
        if (result <= 0D) {
            sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_LOW_AMOUNT, player);
            return false;
        }

        final double newBalance = user.getBalance(targetCurrency) + result;
        if (!targetCurrency.isUnderLimit(newBalance)) {
            targetCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_ERROR_LIMIT_EXCEED, player, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, targetCurrency.format(result))
                    .replace(Placeholders.GENERIC_MAX, targetCurrency.format(targetCurrency.getMaxValue())));
            return false;
        }

        user.removeBalance(sourceCurrency, amount);
        user.addBalance(targetCurrency, result);
        this.userManager.save(user);
        // Custom: publish Redis sync
        this.plugin.getRedisSyncManager().ifPresent(sync -> sync.publishUserBalance(user));

        sourceCurrency.sendPrefixed(Lang.CURRENCY_EXCHANGE_SUCCESS, player, replacer -> replacer
                .replace(Placeholders.GENERIC_BALANCE, sourceCurrency.format(amount))
                .replace(Placeholders.GENERIC_AMOUNT, targetCurrency.format(result)));

        this.logger.addEntry(context, "[%s] %s exchanged %s for %s [%s]. New balances: %s and %s."
                .formatted(
                        sourceCurrency.getId(),
                        user.getName(),
                        sourceCurrency.format(amount),
                        targetCurrency.format(result),
                        targetCurrency.getId(),
                        sourceCurrency.format(user.getBalance(sourceCurrency)),
                        targetCurrency.format(user.getBalance(targetCurrency))));

        return true;
    }
}
