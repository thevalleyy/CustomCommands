package tk.thevalleyy.customcommands;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(
        id = "customcommands",
        name = "CustomCommands",
        version = BuildConstants.VERSION,
        description = "A plugin that allows you to create custom commands.",
        url = "https://github.com/thevalleyy/CustomCommands",
        authors = {"thevalleyy"})

public class CustomCommands {
    private boolean isConfigLoaded = false;
    private static ProxyServer proxy;
    public static Logger logger;

    public static Path folder = null;
    public static String Version = BuildConstants.VERSION;

    public static String Prefix;
    public static String NoPermission;
    public static String NoConsoleCommand;
    public static String CommandExecuted;
    public static String ConfigVersion;

    // Construction
    @Inject
    public CustomCommands(ProxyServer proxy, Logger logger, @DataDirectory final Path folder)
            throws IOException {

        this.proxy = proxy;
        this.logger = logger;
        this.folder = folder;

        // create an instance of ConfigLoader
        ConfigLoader configLoader = new ConfigLoader();

        // load config
        boolean configLoaded = configLoader.loadConfigVariables(folder);
        if (!configLoaded) {
            logger.error("Disabling plugin functionality.");
        } else {
            isConfigLoaded = true;
        }

        // create an instance of registerCustomCommands
        registerCustomCommands registerCustomCommands = new registerCustomCommands();
        Path commandsFolder = Path.of(folder + "/Commands/");

        // create the default command
        registerCustomCommands.createDefaultCommand(commandsFolder);

        // load all custom commands
        registerCustomCommands.loadCustomCommands(commandsFolder);
        // logger.info("CustomCommands has been enabled. (" + BuildConstants.VERSION + ")");
    }

    // Initialization
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!isConfigLoaded) {
            return;
        }

        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta =
                commandManager.metaBuilder("customcommands").aliases("cc").plugin(this).build();
        BrigadierCommand setMainCommand = MainCommand.createBrigadierCommand(proxy);

        commandManager.register(commandMeta, setMainCommand);
    }

    // register commands
    public static void registerCommand(String name, List<String> aliases, BrigadierCommand command) {
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(name).aliases(aliases.stream().toArray(String[]::new)).plugin(proxy).build();

        commandManager.register(commandMeta, command);
    }
}
