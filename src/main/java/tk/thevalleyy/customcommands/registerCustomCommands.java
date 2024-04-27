package tk.thevalleyy.customcommands;

import com.moandjiezana.toml.Toml;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class registerCustomCommands {
    public void createDefaultCommand(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "DefaultCommand.toml");

        if (folder.exists()) return;

        if (folder.mkdirs()) {
            CustomCommands.logger.info("Creating the default command folder.");
        } else {
            CustomCommands.logger.error("Failed to create the default command folder.");
            return;
        }

        try {
            InputStream input = getClass().getResourceAsStream("/DefaultCommand.toml");

            if (input == null) {
                CustomCommands.logger.error("Failed to load the default command.");
                return;
            }

            Files.copy(input, file.toPath());
        } catch (IOException e) {
            CustomCommands.logger.error(e.getMessage());
        }

    }

    // search all toml files in the folder
    public List<Path> searchTomlFiles(Path folder) throws IOException {
        List<Path> tomlFiles = new ArrayList<>();

        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && file.toString().endsWith(".toml")) {
                        tomlFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Handle file visit failure
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            CustomCommands.logger.error(e.getMessage());
        }

        return tomlFiles;
    }

    // load all custom commands
    public void loadCustomCommands(Path folder) {
        try {
            List<Path> tomlFiles = searchTomlFiles(folder);
            for (Path file : tomlFiles) {
                Toml toml;
                try {
                    toml = new Toml().read(file.toFile());
                } catch (RuntimeException e) {
                    CustomCommands.logger.error(e.getMessage());
                    return;
                }


                // does the toml file exist?
                if (toml == null) {
                    CustomCommands.logger.error("Failed to load the command: " + file.getFileName());
                    continue;
                }

                // is the command enabled?
                if (toml.getBoolean("Enabled").equals(false)) continue;
                // CustomCommands.logger.info("Loading: " + file.getFileName() + " (" + toml.getBoolean("Enabled") + ")");


                // get all the values from the toml file
                String name = toml.getString("Name");
                List<String> aliases = toml.getList("Aliases");
                String description = toml.getString("Description");
                String permission = toml.getString("Permission");
                String response = toml.getString("Response");
                long cooldown = toml.getLong("Cooldown");

                // register the command
                try {
                    registerCustomCommands.createBrigadierCommand(name, aliases, description, permission, response, cooldown);
                } catch (Exception e) {
                    CustomCommands.logger.error("Failed to register the command: " + name);
                }
            }
        } catch (IOException e) {
            CustomCommands.logger.error(e.getMessage());
        }
    }

    // create a brigadier command
    public static void createBrigadierCommand(String name, List<String> aliases, String description, String permission, String response, long cooldown) {
        LiteralCommandNode<CommandSource> command =
                LiteralArgumentBuilder.<CommandSource>literal(name)
                        .executes(
                                context -> {
                                    CommandSource player = context.getSource();
                                    if ((!player.hasPermission(permission) && !permission.isEmpty()) && !player.hasPermission("customcommands.admin")) {
                                        player.sendMessage(
                                                MiniMessage.miniMessage().deserialize(CustomCommands.NoPermission));
                                        return 0;
                                    }

                                    player.sendMessage(
                                            MiniMessage.miniMessage()
                                                    .deserialize(CustomCommands.Prefix + response));
                                    return Command.SINGLE_SUCCESS;
                                })
                        .build();

        CustomCommands.registerCommand(name, aliases, new BrigadierCommand(command));
    }
}
