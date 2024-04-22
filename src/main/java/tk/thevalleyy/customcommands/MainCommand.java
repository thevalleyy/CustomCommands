package tk.thevalleyy.customcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class MainCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder.<CommandSource>literal("customcommands")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("subcommand", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String argument;
                            try {
                                argument = ctx.getArgument("subcommand", String.class);
                            } catch (IllegalArgumentException e) {
                                argument = "";
                            }

                            // list with validArguments
                            List<String> validArguments = new ArrayList<>();
                            validArguments.add("reload");
                            validArguments.add("help");
                            validArguments.add("version");

                            // autocomplete already typed argument
                            String finalArgument = argument;
                            validArguments.forEach(cmd -> {
                                try {
                                    if (cmd.startsWith(finalArgument)) {
                                        builder.suggest(cmd,
                                                VelocityBrigadierMessage.tooltip(
                                                        MiniMessage.miniMessage().deserialize(
                                                                "<gray>Run <green>" + cmd + "<gray>")
                                                )
                                        );
                                    }
                                } catch (IllegalArgumentException e) {
                                    // autocomplete all arguments
                                    builder.suggest(cmd,
                                            VelocityBrigadierMessage.tooltip(
                                                    MiniMessage.miniMessage().deserialize(
                                                            "<gray>Run <green>" + cmd + "<gray>")
                                            )
                                    );
                                }
                            });


                            return builder.buildFuture();
                        })
                        .executes(context -> {
//                            if (!(context.getSource() instanceof Player)) {
//                                context.getSource().sendMessage(Component.text(CustomCommands.NoConsoleCommand));
//                                return 0;
//                            }

                            String argumentProvided = context.getArgument("subcommand", String.class);
                            Player player = (Player) context.getSource();

                            // player.sendMessage(Component.text("Running command: " + argumentProvided));

                            // which argument has been provided
                            switch (argumentProvided) {
                                case "reload":
                                    // permission check
                                    if (!player.hasPermission("customcommands.reload") && !player.hasPermission("customcommands.admin")) {
                                        player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.NoPermission));
                                        return 0;
                                    }

                                    // reload config
                                    ConfigLoader configLoader = new ConfigLoader();
                                    boolean configLoaded = configLoader.loadConfigVariables(CustomCommands.folder);
                                    if (!configLoaded) {
                                        player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "<red>Failed to reload the configuration file. <newline> <gray>Check the console for more information."));
                                        return 0;
                                    } else {
                                        player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "<gray>Config reloaded."));
                                    }

                                    break;
                                case "help":
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Help command"));
                                    break;
                                case "version":
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "<hover:show_text:'<gray>Copy to clipboard'><click:copy_to_clipboard:" + CustomCommands.Version + "><gray>v" + CustomCommands.Version));
                                    break;
                                default:
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "<gray>Unknown command."));
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    Player player = (Player) context.getSource();
                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Help command"));

                    return Command.SINGLE_SUCCESS;

                })
                .build();
        return new BrigadierCommand(command);
    }
}
