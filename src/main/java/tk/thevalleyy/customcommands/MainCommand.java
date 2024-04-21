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
                            if (!(context.getSource() instanceof Player)) {
                                context.getSource().sendMessage(Component.text(CustomCommands.NoConsoleCommand));
                                return 0;
                            }

                            String argumentProvided = context.getArgument("subcommand", String.class);
                            Player player = (Player) context.getSource();

                            player.sendMessage(Component.text("Running command: " + argumentProvided));

                            // which argument has been provided
                            switch (argumentProvided) {
                                case "reload":
                                    // todo: implement config reload
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Config reloaded."));
                                    break;
                                case "help":
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Help command"));
                                    break;
                                case "version":
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Version: " + CustomCommands.Version));
                                    break;
                                default:
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Unknown command."));
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    if (!(context.getSource() instanceof Player)) {
                        context.getSource().sendMessage(MiniMessage.miniMessage().deserialize("You must be a player to execute this command."));
                        return 0;
                    }

                    Player player = (Player) context.getSource();
                    player.sendMessage(MiniMessage.miniMessage().deserialize(CustomCommands.Prefix + "Help command"));

                    return Command.SINGLE_SUCCESS;

                })
                .build();
        return new BrigadierCommand(command);
    }
}
