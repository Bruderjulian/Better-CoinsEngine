package su.nightexpress.excellenteconomy.command.currency.provider;

import su.nightexpress.excellenteconomy.ExcellentEconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.Currency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.command.currency.CommandVariant;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommandProvider;
import su.nightexpress.excellenteconomy.command.currency.ProviderNames;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class PaymentsProvider extends CurrencyCommandProvider {

        public PaymentsProvider(final ExcellentEconomyPlugin plugin, final CurrencyRegistry registry,
                        final CurrencyManager manager) {
                super(plugin, registry, manager, ProviderNames.PAYMENTS);
        }

        @Override
        public void buildRoot(final Currency currency, final HubNodeBuilder builder) {
        }

        @Override
        public void build(final Currency currency, final LiteralNodeBuilder builder) {
        }

        @Override
        public void buildHub(final Currency currency, final HubNodeBuilder builder) {
                builder
                                .playerOnly()
                                .permission(Perms.COMMAND_CURRENCY_PAYMENTS)
                                .description(Lang.COMMAND_CURRENCY_PAYMENTS_DESC);

                builder.branch(Commands.literal("toggle", node -> node
                                .withFlags(CommandArguments.FLAG_SILENT)
                                .withArguments(Arguments.playerName(CommandArguments.PLAYER)
                                                .permission(Perms.COMMAND_CURRENCY_PAYMENTS_OTHERS).optional())
                                .executes((context, arguments) -> {
                                        final String name = arguments.contains(CommandArguments.PLAYER)
                                                        ? arguments.get(CommandArguments.PLAYER, String.class)
                                                        : null;

                                        this.manager.managePayments(context.getSender(), name, currency,
                                                        context.hasFlag(CommandArguments.FLAG_SILENT), (value) -> {
                                                                return !value;
                                                        });
                                        return true;
                                })));

                builder.branch(Commands.literal("on", node -> node
                                .withFlags(CommandArguments.FLAG_SILENT)
                                .withArguments(Arguments.playerName(CommandArguments.PLAYER)
                                                .permission(Perms.COMMAND_CURRENCY_PAYMENTS_OTHERS).optional())
                                .executes((context, arguments) -> {
                                        final String name = arguments.contains(CommandArguments.PLAYER)
                                                        ? arguments.get(CommandArguments.PLAYER, String.class)
                                                        : null;

                                        this.manager.managePayments(context.getSender(), name, currency,
                                                        context.hasFlag(CommandArguments.FLAG_SILENT), (value) -> {
                                                                return true;
                                                        });
                                        return true;
                                })));

                builder.branch(Commands.literal("off", node -> node
                                .withFlags(CommandArguments.FLAG_SILENT)
                                .withArguments(Arguments.playerName(CommandArguments.PLAYER)
                                                .permission(Perms.COMMAND_CURRENCY_PAYMENTS_OTHERS).optional())
                                .executes((context, arguments) -> {
                                        final String name = arguments.contains(CommandArguments.PLAYER)
                                                        ? arguments.get(CommandArguments.PLAYER, String.class)
                                                        : null;

                                        this.manager.managePayments(context.getSender(), name, currency,
                                                        context.hasFlag(CommandArguments.FLAG_SILENT), (value) -> {
                                                                return false;
                                                        });
                                        return true;
                                })));
        }

        @Override
        public boolean isHubCommand() {
                return true;
        }

        @Override
        public boolean isAvailable(final Currency currency) {
                return currency.isTransferAllowed();
        }

        @Override

        public CommandDefinition getDefaultDefinition() {
                return new CommandDefinition(CommandVariant.enabled("payments"), CommandVariant.enabled("payments"));
        }
}
