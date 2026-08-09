package su.nightexpress.excellenteconomy.command.plugin;

import java.util.HashSet;
import java.util.Set;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.config.Config;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.Lists;

public class PluginCommands extends SimpleManager<ExcellentEconomyPlugin> {

    private final CurrencyManager currencyManager;
    private final CurrencyRegistry currencyRegistry;

    private final Set<NightCommand> commands;

    public PluginCommands(final ExcellentEconomyPlugin plugin, final CurrencyRegistry currencyRegistry,
            final CurrencyManager currencyManager) {
        super(plugin);
        this.currencyManager = currencyManager;
        this.currencyRegistry = currencyRegistry;
        this.commands = new HashSet<>();
    }

    @Override
    protected void onLoad() {
        this.loadAdminCommands();
        this.loadGlobalCommands();
    }

    @Override
    protected void onShutdown() {
        this.commands.forEach(NightCommand::unregister);
        this.commands.clear();
    }

    private void loadAdminCommands() {
        this.registerCommand(NightCommand.forPlugin(this.plugin, builder -> {
            builder.branch(Commands.literal("reload")
                    .description(CoreLang.COMMAND_RELOAD_DESC)
                    .permission(Perms.COMMAND_RELOAD)
                    .executes((context, arguments) -> {
                        this.plugin.doReload(context.getSender());
                        return true;
                    }));
            builder.branch(Commands.literal("create")
                    .permission(Perms.COMMAND_CREATE)
                    .description(Lang.COMMAND_CREATE_DESC)
                    .withArguments(
                            Arguments.string(CommandArguments.NAME).localized(CoreLang.COMMAND_ARGUMENT_NAME_NAME),
                            Arguments.string(CommandArguments.SYMBOL).localized(Lang.COMMAND_ARGUMENT_NAME_SYMBOL),
                            Arguments.bool(CommandArguments.DECIMALS).localized(Lang.COMMAND_ARGUMENT_NAME_DECIMAL)
                                    .optional().suggestions((reader, context) -> Lists.newList("true", "false")))
                    .executes(this::createCurrency));
            builder.branch(Commands.literal("migrate")
                    .permission(Perms.COMMAND_MIGRATE)
                    .description(Lang.COMMAND_MIGRATE_DESC)
                    .withArguments(
                            Arguments.string(CommandArguments.NAME).localized(Lang.COMMAND_ARGUMENT_NAME_PLUGIN)
                                    .suggestions(
                                            (reader, context) -> this.plugin.getMigrationManager().get()
                                                    .getMigratorNames()),
                            CommandArguments.currency(this.currencyRegistry))
                    .executes(this::migrate));
        }));
    }

    private void loadGlobalCommands() {
        if (Config.isWalletEnabled()) {
            this.registerCommand(NightCommand.literal(this.plugin, Config.WALLET_ALIASES.get(), builder -> builder
                    .description(Lang.COMMAND_WALLET_DESC)
                    .permission(Perms.COMMAND_WALLET)
                    .withArguments(Arguments.playerName(CommandArguments.PLAYER).permission(Perms.COMMAND_WALLET_OTHERS)
                            .optional())
                    .executes(this::showWallet)));
        }
    }

    private void registerCommand(final NightCommand command) {
        if (command.register()) {
            this.commands.add(command);
        }
    }

    private boolean createCurrency(final CommandContext context, final ParsedArguments arguments) {
        final String name = arguments.getString(CommandArguments.NAME);
        final String symbol = arguments.getString(CommandArguments.SYMBOL);
        final boolean decimals = arguments.getBoolean(CommandArguments.DECIMALS, true);

        return this.currencyManager.createCurrency(context.getSender(), name, symbol, decimals);
    }

    private boolean showWallet(final CommandContext context, final ParsedArguments arguments) {
        final String name = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
        this.currencyManager.showWallet(context.getSender(), name);
        return true;
    }

    private boolean migrate(final CommandContext context, final ParsedArguments arguments) {
        return this.plugin.getMigrationManager().get().startMigration(
                context.getSender(),
                arguments.getString(CommandArguments.NAME),
                arguments.get(CommandArguments.CURRENCY, Currency.class));
    }
}
