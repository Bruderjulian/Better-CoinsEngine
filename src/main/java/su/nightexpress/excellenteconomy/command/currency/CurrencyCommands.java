package su.nightexpress.excellenteconomy.command.currency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.currency.provider.*;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.LowerCase;

public class CurrencyCommands extends SimpleManager<ExcellentEconomyPlugin> {

    private final CurrencyRegistry currencyRegistry;
    private final CurrencyManager currencyManager;

    private final Map<String, CommandProvider> providerByNameMap;
    private final Map<String, CommandDefinition> definitionByNameMap;
    private final Map<String, Set<NightCommand>> currencyCommands;

    public CurrencyCommands(ExcellentEconomyPlugin plugin, CurrencyRegistry currencyRegistry,
            CurrencyManager currencyManager) {
        super(plugin);
        this.currencyRegistry = currencyRegistry;
        this.currencyManager = currencyManager;
        this.providerByNameMap = new HashMap<>();
        this.definitionByNameMap = new HashMap<>();
        this.currencyCommands = new HashMap<>();

        this.registerDefaultProviders();
    }

    @Override
    protected void onLoad() {
        this.loadCommands();
    }

    @Override
    protected void onShutdown() {
        this.currencyRegistry.getCurrencies().forEach(this::unregisterCommands);
        this.currencyCommands.values().forEach(set -> set.forEach(NightCommand::unregister));
        this.currencyCommands.clear();

        this.definitionByNameMap.clear();
        this.providerByNameMap.clear();
    }

    private void registerDefaultProviders() {
        this.registerProvider(new BankProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new BalanceProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new PayProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new PaymentsProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new GiveProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new GiveAllProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new SetProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new RemoveProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new RemoveAllProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new ExchangeProvider(this.plugin, this.currencyRegistry, this.currencyManager));
        this.registerProvider(new TopCommandProvider(this.plugin, this.plugin.getTopManager().get()));
    }

    public void registerProvider(CommandProvider provider) {
        this.providerByNameMap.put(provider.getName(), provider);
    }

    private void loadCommands() {
        this.loadCommandDefinitions();
        this.currencyRegistry.getCurrencies().forEach(this::loadCommands);
    }

    private void loadCommandDefinitions() {
        FileConfig config = FileConfig.load(this.plugin.getDataFolder().getPath(), "commands.yml");
        String path = "Commands.";

        if (config.getSection(path).isEmpty()) {
            this.providerByNameMap
                    .forEach((name, provider) -> config.set(path + "." + name, provider.getDefaultDefinition()));
        }

        config.getSection(path).forEach(sId -> {
            String name = LowerCase.INTERNAL.apply(sId);

            if (!this.providerByNameMap.containsKey(name)) {
                this.plugin.warn("Unknown command '" + sId + "' in '" + config.getPath() + "'.");
                return;
            }

            CommandDefinition definition = CommandDefinition.read(config, path + "." + sId);
            this.definitionByNameMap.put(name, definition);
        });

        config.saveChanges();
    }

    public void loadEcoCommands(HubNodeBuilder builder) {
        this.currencyRegistry.findPrimary().ifPresent(primary -> {
            this.providerByNameMap.forEach((name, provider) -> {
                if (!provider.isAvailable(primary)) {
                    return;
                }

                if (provider.isEcoCommand()) {
                    String ecoName = provider.getEcoCommandName();
                    if (ecoName == null) {
                        return;
                    }

                    builder.branch(Commands.literal(ecoName, literal -> provider.buildEco(primary, literal)));
                    return;
                }

                if (ProviderNames.PAYMENTS.equals(name)) {
                    builder.branch(Commands.literal("payments", literal -> provider.buildEco(primary, literal)));
                }
            });
        });
    }

    public void loadCommands(Currency currency) {
        NightCommand currencyCommand = NightCommand.hub(this.plugin, currency.getCommandAliases(), rootBuilder -> {
            rootBuilder.permission(currency.isPermissionRequired() ? currency.getPermission() : null);
            rootBuilder.description(currency.replacePlaceholders().apply(Lang.COMMAND_CURRENCY_ROOT_DESC.text()));

            this.providerByNameMap.forEach((name, provider) -> {
                CommandDefinition definition = this.definitionByNameMap.getOrDefault(name,
                        provider.getDefaultDefinition());
                CommandVariant children = definition.children();
                CommandVariant dedicated = definition.dedicated();

                if (!children.enabled() && !dedicated.enabled()) {
                    return;
                }
                if (!provider.isAvailable(currency)) {
                    return;
                }
                provider.buildRoot(currency, rootBuilder);

                if (children.enabled()) {
                    for (String alias : children.aliases()) {
                        if (provider.isHubCommand()) {
                            rootBuilder
                                    .branch(Commands.hub(alias, literal -> provider.buildHub(currency, literal)));
                        } else {
                            rootBuilder.branch(Commands.literal(alias,
                                    literal -> provider.build(currency, literal)));
                        }

                    }
                }
                if (dedicated.enabled() && currency.isPrimary()) {
                    if (provider.isHubCommand()) {
                        NightCommand command = NightCommand.hub(this.plugin, dedicated.aliases(),
                                hub -> provider.buildHub(currency, hub));
                        this.registerCommand(currency, command);
                    } else {
                        NightCommand command = NightCommand.literal(this.plugin, dedicated.aliases(),
                                literal -> provider.build(currency, literal));
                        this.registerCommand(currency, command);
                    }
                }
            });
        });

        this.registerCommand(currency, currencyCommand);
    }

    private void registerCommand(Currency currency, NightCommand command) {
        if (command.register()) {
            this.currencyCommands.computeIfAbsent(currency.getId(), k -> new HashSet<>()).add(command);
        }
    }

    public void unregisterCommands(Currency currency) {
        Set<NightCommand> commands = this.currencyCommands.remove(currency.getId());
        if (commands != null) {
            commands.forEach(NightCommand::unregister);
        }
    }
}
