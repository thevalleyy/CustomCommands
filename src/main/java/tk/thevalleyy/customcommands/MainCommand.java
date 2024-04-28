package tk.thevalleyy.customcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        // list with all commands, description and permission (empty string for no permission)
        List<List<String>> commands = new ArrayList<>();
        commands.add(List.of("reload", "Reload the configuration file", "customcommands.reload"));
        commands.add(List.of("help", "Show the help page", "customcommands.help"));
        commands.add(List.of("version", "Show the plugins version", "customcommands.version"));
        commands.add(List.of("list", "List all custom commands", "customcommands.list"));

        // generate help list for the help command
        StringBuilder helpList = new StringBuilder();
        for (List<String> cmd : commands) {
            helpList
                    .append("<hover:show_text:'<gray>Click to run'><click:suggest_command:'/cc ")
                    .append(cmd.get(0))
                    .append("'>")
                    .append("<yellow>/cc ")
                    .append(cmd.get(0))
                    .append("</click></hover>")
                    .append("<gray> - <hover:show_text:'<gray>Permission: ")
                    .append("<green>")
                    .append(cmd.get(2).isEmpty() ? "none" : cmd.get(2))
                    .append("'>")
                    .append(cmd.get(1))
                    .append(" <gray></hover>")
                    .append("\n");
        }

        LiteralCommandNode<CommandSource> command =
                LiteralArgumentBuilder.<CommandSource>literal("customcommands")
                        .then(
                                RequiredArgumentBuilder.<CommandSource, String>argument(
                                                "subcommand", StringArgumentType.word())
                                        .suggests(
                                                (ctx, builder) -> {
                                                    String argument;
                                                    try {
                                                        argument = ctx.getArgument("subcommand", String.class);
                                                    } catch (IllegalArgumentException e) {
                                                        argument = "";
                                                    }

                                                    // autocomplete already typed argument
                                                    String finalArgument = argument;
                                                    commands.forEach(
                                                            cmd -> {
                                                                try {
                                                                    if (cmd.get(0).startsWith(finalArgument)) {
                                                                        builder.suggest(
                                                                                cmd.get(0),
                                                                                VelocityBrigadierMessage.tooltip(
                                                                                        MiniMessage.miniMessage()
                                                                                                .deserialize("<gray>" + cmd.get(1))));
                                                                    }
                                                                } catch (IllegalArgumentException e) {
                                                                    // autocomplete all arguments
                                                                    builder.suggest(
                                                                            cmd.get(0),
                                                                            VelocityBrigadierMessage.tooltip(
                                                                                    MiniMessage.miniMessage()
                                                                                            .deserialize("<gray>" + cmd.get(1))));
                                                                }
                                                            });

                                                    return builder.buildFuture();
                                                })
                                        .executes(
                                                context -> {
                                                    String argumentProvided = context.getArgument("subcommand", String.class);
                                                    CommandSource player = context.getSource(); // no console check necessary.

                                                    boolean commandFound = false;
                                                    for (List<String> cmd : commands) {
                                                        String commandName = cmd.get(0);
                                                        String description = cmd.get(1);
                                                        String permission = cmd.get(2);

                                                        if (commandName.equalsIgnoreCase(argumentProvided)) {
                                                            commandFound = true;
                                                            if ((!player.hasPermission(permission) && !permission.isEmpty())
                                                                    && !player.hasPermission("customcommands.admin")) {
                                                                player.sendMessage(
                                                                        MiniMessage.miniMessage()
                                                                                .deserialize(CustomCommands.NoPermission));
                                                                return 0;
                                                            }

                                                            switch (commandName) {
                                                                case "reload": // reload the configuration file
                                                                    ConfigLoader configLoader = new ConfigLoader();
                                                                    boolean configLoaded =
                                                                            configLoader.loadConfigVariables(CustomCommands.folder);
                                                                    if (!configLoaded) {
                                                                        player.sendMessage(
                                                                                MiniMessage.miniMessage()
                                                                                        .deserialize(
                                                                                                CustomCommands.Prefix
                                                                                                        + "<red>Failed to reload the configuration file. <newline> <gray>Check the console for more information."));
                                                                        return 0;
                                                                    } else {
                                                                        player.sendMessage(
                                                                                MiniMessage.miniMessage()
                                                                                        .deserialize(
                                                                                                CustomCommands.Prefix + "<gray>Config reloaded."));
                                                                    }

                                                                    // create an instance of registerCustomCommands
                                                                    registerCustomCommands registerCustomCommands = new registerCustomCommands();
                                                                    Path commandsFolder = Path.of(CustomCommands.folder + "/Commands/");

                                                                    try {
                                                                        // create the default command
                                                                        registerCustomCommands.createDefaultCommand(commandsFolder);

                                                                        // load all custom commands
                                                                        registerCustomCommands.loadCustomCommands(commandsFolder);
                                                                    } catch (Exception e) {
                                                                        player.sendMessage(
                                                                                MiniMessage.miniMessage()
                                                                                        .deserialize(
                                                                                                CustomCommands.Prefix
                                                                                                        + "<red>Failed to reload the custom commands. <newline> <gray>Check the console for more information."));
                                                                        return 0;
                                                                    }

                                                                    player.sendMessage(
                                                                            MiniMessage.miniMessage()
                                                                                    .deserialize(
                                                                                            CustomCommands.Prefix + "<gray>Custom commands reloaded."));
                                                                    
                                                                    break;

                                                                case "help": // display the dynamic generated help message
                                                                    player.sendMessage(
                                                                            MiniMessage.miniMessage()
                                                                                    .deserialize(
                                                                                            CustomCommands.Prefix
                                                                                                    + "<newline>"
                                                                                                    + helpList.toString()));
                                                                    break;

                                                                case "version": // display the plugin version
                                                                    player.sendMessage(
                                                                            MiniMessage.miniMessage()
                                                                                    .deserialize(
                                                                                            CustomCommands.Prefix
                                                                                                    + "<hover:show_text:'<gray>Copy to clipboard'><click:copy_to_clipboard:"
                                                                                                    + CustomCommands.Version
                                                                                                    + "><gray>v"
                                                                                                    + CustomCommands.Version));
                                                                    break;

                                                                case "list": // list all custom commands
                                                                    player.sendMessage(
                                                                            MiniMessage.miniMessage()
                                                                                    .deserialize(
                                                                                            CustomCommands.Prefix + "<gray>Custom commands:"));
                                                                    // todo: list all custom commands
                                                                    break;

                                                                default: // unknown command
                                                                    player.sendMessage(
                                                                            MiniMessage.miniMessage()
                                                                                    .deserialize(
                                                                                            CustomCommands.Prefix + "<gray>Unknown command."));
                                                                    break;
                                                            }
                                                        }
                                                    }

                                                    if (!commandFound) {
                                                        player.sendMessage(
                                                                MiniMessage.miniMessage()
                                                                        .deserialize(CustomCommands.Prefix + "<gray>Unknown command."));
                                                        return 0;
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                }))
                        .executes(
                                context -> {
                                    CommandSource player = context.getSource();
                                    if (!player.hasPermission("customcommands.help")
                                            && !player.hasPermission("customcommands.admin")) {
                                        player.sendMessage(
                                                MiniMessage.miniMessage().deserialize(CustomCommands.NoPermission));
                                        return 0;
                                    }

                                    player.sendMessage(
                                            MiniMessage.miniMessage()
                                                    .deserialize(CustomCommands.Prefix + "<newline>" + helpList.toString()));
                                    return Command.SINGLE_SUCCESS;
                                })
                        .build();
        return new BrigadierCommand(command);
    }
}
