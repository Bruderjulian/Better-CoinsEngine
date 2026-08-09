package su.nightexpress.excellenteconomy.currency.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.currency.operation.OperationResult;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.data.impl.CoinsUser;
import su.nightexpress.excellenteconomy.user.UserManager;

@SuppressWarnings("deprecation")
public class EconomyCurrency extends AbstractCurrency implements Economy {

    private final ExcellentEconomyPlugin plugin;
    private final File banksFile;
    private final YamlConfiguration banksConfig;
    /*
     * private final CurrencyManager currencyManager;
     * private final DataHandler dataHandler;
     * private final UserManager userManager;
     */

    public EconomyCurrency(Path path,
            String id,
            ExcellentEconomyPlugin plugin,
            CurrencyManager currencyManager,
            DataHandler dataHandler,
            UserManager userManager) {
        super(path, id);

        this.plugin = plugin;
        this.banksFile = new File(plugin.getDataFolder(), "banks.yml");
        this.banksConfig = YamlConfiguration.loadConfiguration(this.banksFile);
        /*
         * this.currencyManager = currencyManager;
         * this.dataHandler = dataHandler;
         * this.userManager = userManager;
         */
    }

    @Override
    public void onRegister() {
        ServicesManager services = Bukkit.getServer().getServicesManager();
        services.register(Economy.class, this, this.plugin, ServicePriority.High);
    }

    @Override
    public void onUnregister() {
        ServicesManager services = Bukkit.getServer().getServicesManager();
        services.unregister(Economy.class, this);
    }

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String currencyNamePlural() {
        return this.getName();
    }

    @Override
    public String currencyNameSingular() {
        return this.getName();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return this.createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return this.getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        return this.getBalance(user);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return this.getBalance(playerName);
    }

    @Override
    public double getBalance(String playerName) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(playerName);
        return this.getBalance(user);
    }

    private double getBalance(CoinsUser user) {
        return user == null ? 0D : user.getBalance(this);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return this.hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return this.plugin.getDataHandler().isUserExists(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return this.hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return this.plugin.getDataHandler().isUserExists(playerName);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return this.has(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        return this.has(user, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return this.has(playerName, amount);
    }

    @Override
    public boolean has(String playerName, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(playerName);
        return this.has(user, amount);
    }

    private boolean has(CoinsUser user, double amount) {
        return user != null && user.hasEnough(this, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return this.depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        return this.depositUser(user, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return this.depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(playerName);
        return this.depositUser(user, amount);
    }

    private EconomyResponse depositUser(CoinsUser user, double amount) {
        if (user == null) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE,
                    Lang.ECONOMY_ERROR_INVALID_PLAYER.text());
        }

        OperationResult result = this.plugin.getCurrencyManager().give(this.operationContext(), user, this, amount);
        EconomyResponse.ResponseType type = result == OperationResult.SUCCESS ? EconomyResponse.ResponseType.SUCCESS
                : EconomyResponse.ResponseType.FAILURE;

        return new EconomyResponse(amount, user.getBalance(this), type, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return this.withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        return this.withdrawUser(user, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return this.withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        CoinsUser user = this.plugin.getUserManager().getOrFetch(playerName);
        return this.withdrawUser(user, amount);
    }

    private EconomyResponse withdrawUser(CoinsUser user, double amount) {
        if (user == null) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE,
                    Lang.ECONOMY_ERROR_INVALID_PLAYER.text());
        }

        if (!user.hasEnough(this, amount)) {
            return new EconomyResponse(amount, user.getBalance(this), EconomyResponse.ResponseType.FAILURE,
                    Lang.ECONOMY_ERROR_INSUFFICIENT_FUNDS.text());
        }

        OperationResult result = this.plugin.getCurrencyManager().remove(this.operationContext(), user, this, amount);
        EconomyResponse.ResponseType type = result == OperationResult.SUCCESS ? EconomyResponse.ResponseType.SUCCESS
                : EconomyResponse.ResponseType.FAILURE;

        return new EconomyResponse(amount, user.getBalance(this), type, null);
    }

    private OperationContext operationContext() {
        return OperationContext.custom("Vault Eco - " + this.name).silentFor(NotificationTarget.EXECUTOR,
                NotificationTarget.USER, NotificationTarget.CONSOLE_LOGGER);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        if (player == null || player.isBlank()) {
            return bankFailure("Invalid bank owner.");
        }

        return this.createBank(name, player.trim(), null);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        if (player == null) {
            return bankFailure("Invalid bank owner.");
        }

        return this.createBank(name, player.getName() == null ? player.getUniqueId().toString() : player.getName(),
                player.getUniqueId());
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        String bankId = bankId(name);
        if (bankId == null || !this.banksConfig.isConfigurationSection("banks." + bankId)) {
            return bankFailure("Bank does not exist.");
        }

        this.banksConfig.set("banks." + bankId, null);
        return this.saveBanks() ? bankSuccess(0D, 0D) : bankFailure("Could not save bank data.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        Bank bank = this.getBank(name);
        if (bank == null) {
            return bankFailure("Bank does not exist.");
        }
        if (amount < 0D) {
            return bankFailure("Amount must not be negative.");
        }

        return new EconomyResponse(amount, bank.balance(),
                bank.balance() >= amount ? EconomyResponse.ResponseType.SUCCESS
                        : EconomyResponse.ResponseType.FAILURE,
                null);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        Bank bank = this.getBank(name);
        if (bank == null) {
            return bankFailure("Bank does not exist.");
        }
        if (!isValidAmount(amount)) {
            return bankFailure("Amount must be positive and finite.");
        }
        if (bank.balance() < amount) {
            return new EconomyResponse(amount, bank.balance(), EconomyResponse.ResponseType.FAILURE,
                    Lang.ECONOMY_ERROR_INSUFFICIENT_FUNDS.text());
        }

        return this.setBankBalance(bank, bank.balance() - amount, amount);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        Bank bank = this.getBank(name);
        if (bank == null) {
            return bankFailure("Bank does not exist.");
        }
        if (!isValidAmount(amount)) {
            return bankFailure("Amount must be positive and finite.");
        }

        return this.setBankBalance(bank, bank.balance() + amount, amount);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        Bank bank = this.getBank(name);
        if (bank == null || player == null) {
            return bankFailure("Bank or player does not exist.");
        }

        return membershipResponse(bank, bank.ownerUuid().equals(player.getUniqueId().toString()));
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        Bank bank = this.getBank(name);
        if (bank == null || playerName == null) {
            return bankFailure("Bank or player does not exist.");
        }

        return membershipResponse(bank, bank.ownerName().equals(normalizeName(playerName)));
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        Bank bank = this.getBank(name);
        if (bank == null || player == null) {
            return bankFailure("Bank or player does not exist.");
        }

        return membershipResponse(bank, bank.ownerUuid().equals(player.getUniqueId().toString())
                || bank.members().contains(player.getUniqueId().toString()));
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        Bank bank = this.getBank(name);
        if (bank == null || playerName == null) {
            return bankFailure("Bank or player does not exist.");
        }

        return membershipResponse(bank, bank.ownerName().equals(normalizeName(playerName))
                || bank.memberNames().contains(normalizeName(playerName)));
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        Bank bank = this.getBank(name);
        return bank == null ? bankFailure("Bank does not exist.") : bankSuccess(0D, bank.balance());
    }

    @Override
    public List<String> getBanks() {
        ConfigurationSection section = this.banksConfig.getConfigurationSection("banks");
        if (section == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    @Override
    public boolean hasBankSupport() {
        return true;
    }

    public EconomyResponse depositBank(Player player, String name, double amount) {
        Bank bank = this.getBank(name);
        if (bank == null) {
            return bankFailure("Bank does not exist.");
        }
        if (!isBankMember(bank, player)) {
            return bankFailure("You are not a member of this bank.");
        }
        if (!isValidAmount(amount) || !this.has(player, amount)) {
            return bankFailure("You do not have enough funds.");
        }

        EconomyResponse response = this.bankDeposit(name, amount);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            return response;
        }

        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        if (this.plugin.getCurrencyManager().remove(this.operationContext(), user, this,
                amount) == OperationResult.SUCCESS) {
            return response;
        }

        this.bankWithdraw(name, amount);
        return bankFailure("Could not withdraw funds from your account.");
    }

    public EconomyResponse withdrawBank(Player player, String name, double amount) {
        Bank bank = this.getBank(name);
        if (bank == null) {
            return bankFailure("Bank does not exist.");
        }
        if (!isBankOwner(bank, player)) {
            return bankFailure("Only the bank owner can withdraw funds.");
        }

        EconomyResponse response = this.bankWithdraw(name, amount);
        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            return response;
        }

        CoinsUser user = this.plugin.getUserManager().getOrFetch(player.getUniqueId());
        if (this.plugin.getCurrencyManager().give(this.operationContext(), user, this,
                amount) == OperationResult.SUCCESS) {
            return response;
        }

        this.bankDeposit(name, amount);
        return bankFailure("Could not deposit funds into your account.");
    }

    public boolean isBankOwner(String name, Player player) {
        Bank bank = this.getBank(name);
        return bank != null && isBankOwner(bank, player);
    }

    private static boolean isBankOwner(Bank bank, OfflinePlayer player) {
        return !bank.ownerUuid().isEmpty() && bank.ownerUuid().equals(player.getUniqueId().toString());
    }

    private static boolean isBankMember(Bank bank, OfflinePlayer player) {
        return isBankOwner(bank, player) || bank.members().contains(player.getUniqueId().toString());
    }

    private EconomyResponse createBank(String name, String ownerName, UUID ownerUuid) {
        String bankId = bankId(name);
        if (bankId == null) {
            return bankFailure("Bank name must not be blank.");
        }
        if (this.banksConfig.isConfigurationSection("banks." + bankId)) {
            return bankFailure("A bank with this name already exists.");
        }

        String path = "banks." + bankId;
        this.banksConfig.set(path + ".name", name.trim());
        this.banksConfig.set(path + ".owner-name", normalizeName(ownerName));
        this.banksConfig.set(path + ".owner-uuid", ownerUuid == null ? "" : ownerUuid.toString());
        this.banksConfig.set(path + ".members", Collections.emptyList());
        this.banksConfig.set(path + ".member-names", Collections.emptyList());
        this.banksConfig.set(path + ".balance", 0D);
        return this.saveBanks() ? bankSuccess(0D, 0D) : bankFailure("Could not save bank data.");
    }

    private Bank getBank(String name) {
        String bankId = bankId(name);
        if (bankId == null) {
            return null;
        }
        ConfigurationSection section = this.banksConfig.getConfigurationSection("banks." + bankId);
        return section == null ? null
                : new Bank(bankId, section.getDouble("balance"), section.getString("owner-name", ""),
                        section.getString("owner-uuid", ""), section.getStringList("members"),
                        section.getStringList("member-names"));
    }

    private EconomyResponse setBankBalance(Bank bank, double balance, double amount) {
        this.banksConfig.set("banks." + bank.id() + ".balance", balance);
        return this.saveBanks() ? bankSuccess(amount, balance) : bankFailure("Could not save bank data.");
    }

    private boolean saveBanks() {
        try {
            this.banksConfig.save(this.banksFile);
            return true;
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Unable to save banks.yml: " + exception.getMessage());
            return false;
        }
    }

    private static String bankId(String name) {
        return name == null || name.isBlank() ? null
                : Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(name.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isValidAmount(double amount) {
        return Double.isFinite(amount) && amount > 0D;
    }

    private static EconomyResponse bankSuccess(double amount, double balance) {
        return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    private static EconomyResponse bankFailure(String message) {
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, message);
    }

    private static EconomyResponse membershipResponse(Bank bank, boolean member) {
        return new EconomyResponse(0D, bank.balance(), member ? EconomyResponse.ResponseType.SUCCESS
                : EconomyResponse.ResponseType.FAILURE, null);
    }

    private record Bank(String id, double balance, String ownerName, String ownerUuid, List<String> members,
            List<String> memberNames) {
    }
}
