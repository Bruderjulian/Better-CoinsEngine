package su.nightexpress.excellenteconomy.command.currency.provider;

import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;
import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommandProvider;
import su.nightexpress.excellenteconomy.command.currency.ProviderNames;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.currency.impl.EconomyCurrency;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class BankProvider extends CurrencyCommandProvider {

  public BankProvider(final ExcellentEconomyPlugin plugin, final CurrencyRegistry registry,
      final CurrencyManager manager) {
    super(plugin, registry, manager, ProviderNames.BANK);
  }

  @Override
  public void buildRoot(final Currency currency, final HubNodeBuilder builder) {
  }

  @Override
  public void build(final Currency currency, final LiteralNodeBuilder builder) {
  }

  @Override
  public void buildHub(final Currency currency, final HubNodeBuilder builder) {
    final EconomyCurrency economy = (EconomyCurrency) currency;

    builder.playerOnly().permission(Perms.COMMAND_CURRENCY_BANK);

    builder.branch(Commands.literal("create", node -> node
        .withArguments(Arguments.string(CommandArguments.NAME))
        .executes((context, arguments) -> execute(context.getPlayerOrThrow(), economy, currency, "create",
            arguments.getString(CommandArguments.NAME), 0D))));

    builder.branch(Commands.literal("balance", node -> node
        .withArguments(Arguments.string(CommandArguments.NAME))
        .executes((context, arguments) -> execute(context.getPlayerOrThrow(), economy, currency, "balance",
            arguments.getString(CommandArguments.NAME), 0D))));

    builder.branch(Commands.literal("deposit", node -> node
        .withArguments(
            Arguments.string(CommandArguments.NAME),
            CommandArguments.amount())
        .executes((context, arguments) -> execute(context.getPlayerOrThrow(), economy, currency, "deposit",
            arguments.getString(CommandArguments.NAME),
            arguments.getDouble(CommandArguments.AMOUNT)))));

    builder.branch(Commands.literal("withdraw", node -> node
        .withArguments(
            Arguments.string(CommandArguments.NAME),
            CommandArguments.amount())
        .executes((context, arguments) -> execute(context.getPlayerOrThrow(), economy, currency, "withdraw",
            arguments.getString(CommandArguments.NAME),
            arguments.getDouble(CommandArguments.AMOUNT)))));

    builder.branch(Commands.literal("delete", node -> node
        .withArguments(Arguments.string(CommandArguments.NAME))
        .executes((context, arguments) -> execute(context.getPlayerOrThrow(), economy, currency, "delete",
            arguments.getString(CommandArguments.NAME), 0D))));
  }

  private static boolean execute(final Player player, final EconomyCurrency economy, final Currency currency,
      final String action, final String name,
      final double amount) {
    return switch (action.toLowerCase(java.util.Locale.ROOT)) {
      case "create" -> respond(player, economy.createBank(name, (org.bukkit.OfflinePlayer) player), currency);
      case "balance" -> respond(player, economy.bankBalance(name), currency);
      case "deposit" -> respond(player, economy.depositBank(player, name, amount), currency);
      case "withdraw" -> respond(player, economy.withdrawBank(player, name, amount), currency);
      case "delete" -> delete(player, economy, name, currency);
      default -> {
        player.sendMessage("Unknown bank action!");
        yield false;
      }
    };
  }

  private static boolean delete(final Player player, final EconomyCurrency economy, final String name,
      final Currency currency) {
    if (!economy.isBankOwner(name, player)) {
      player.sendMessage("You must be the bank owner to delete it.");
      return false;
    }
    return respond(player, economy.deleteBank(name), currency);
  }

  @SuppressWarnings("deprecation")
  private static boolean respond(final Player player, final EconomyResponse response, final Currency currency) {
    if (response.type == EconomyResponse.ResponseType.SUCCESS) {
      player.sendMessage("Bank balance: " + currency.format(response.balance));
      return true;
    }
    player.sendMessage(response.errorMessage == null ? "Bank operation failed." : response.errorMessage);
    return false;
  }

  @Override
  public boolean isAvailable(final Currency currency) {
    return currency instanceof EconomyCurrency && currency.isPrimary();
  }

  @Override
  public boolean isHubCommand() {
    return true;
  }

  @Override

  public CommandDefinition getDefaultDefinition() {
    return new CommandDefinition(CommandVariant.enabled("bank"), CommandVariant.enabled("banks"));
  }

}
