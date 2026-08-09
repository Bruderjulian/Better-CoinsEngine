package su.nightexpress.excellenteconomy.command.plugin;

import java.util.HashSet;
import java.util.Set;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.CommandNames;
import su.nightexpress.excellenteconomy.config.Config;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
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

    private final Set<CommandProvider> providers;
    private final Set<NightCommand> commands;

    public PluginCommands(ExcellentEconomyPlugin plugin, CurrencyManager currencyManager) {
        super(plugin);
        this.currencyManager = currencyManager;
        this.providers = new HashSet<>();
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

    public void registerProvider(CommandProvider provider) {
        this.providers.add(provider);
    }

    private void loadAdminCommands() {
        this.registerCommand(NightCommand.forPlugin(this.plugin, builder -> {
            builder.branch(Commands.literal(CommandNames.ADMIN_RELOAD)
                    .description(CoreLang.COMMAND_RELOAD_DESC)
                    .permission(Perms.COMMAND_RELOAD)
                    .executes((context, arguments) -> {
                        this.plugin.doReload(context.getSender());
                        return true;
                    }));
            builder.branch(Commands.literal(CommandNames.ADMIN_CREATE)
                    .permission(Perms.COMMAND_CREATE)
                    .description(Lang.COMMAND_CREATE_DESC)
                    .withArguments(
                            Arguments.string(CommandArguments.NAME).localized(CoreLang.COMMAND_ARGUMENT_NAME_NAME),
                            Arguments.string(CommandArguments.SYMBOL).localized(Lang.COMMAND_ARGUMENT_NAME_SYMBOL),
                            Arguments.bool(CommandArguments.DECIMALS).localized(Lang.COMMAND_ARGUMENT_NAME_DECIMAL)
                                    .optional().suggestions((reader, context) -> Lists.newList("true", "false")))
                    .executes(this::createCurrency));

            this.providers.forEach(provider -> provider.build(builder));
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

    private void registerCommand(NightCommand command) {
        if (command.register()) {
            this.commands.add(command);
        }
    }

    private boolean createCurrency(CommandContext context, ParsedArguments arguments) {
        String name = arguments.getString(CommandArguments.NAME);
        String symbol = arguments.getString(CommandArguments.SYMBOL);
        boolean decimals = arguments.getBoolean(CommandArguments.DECIMALS, true);

        return this.currencyManager.createCurrency(context.getSender(), name, symbol, decimals);
    }

    private boolean showWallet(CommandContext context, ParsedArguments arguments) {
        String name = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
        this.currencyManager.showWallet(context.getSender(), name);
        return true;
    }
}
