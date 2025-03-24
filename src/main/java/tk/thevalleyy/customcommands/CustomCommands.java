package tk.thevalleyy.customcommands;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = "customcommands",
        name = "CustomCommands",
        version = BuildConstants.VERSION,
        description = "A plugin that allows you to create custom commands.",
        url = "https://github.com/thevalleyy/CustomCommands",
        authors = {"thevalleyy"})

public class CustomCommands {
    private boolean isLoadedCorrectly = true;
    private static ProxyServer proxy;
    public static Logger logger;

    public static Path folder = null;
    public static String Version = BuildConstants.VERSION;

    public static String Prefix;
    public static String NoPermission;
    public static String NoConsoleCommand;
    public static String CommandExecuted;
    public static String ConfigVersion;
    private static CustomCommands instance;

    // Construction
    @Inject
    public CustomCommands(ProxyServer proxy, Logger logger, @DataDirectory final Path folder) throws IOException {
        CustomCommands.proxy = proxy;
        CustomCommands.logger = logger;
        CustomCommands.folder = folder;
        instance = this;

        System.out.print(proxy);
        System.out.print(logger);
        System.out.print(folder);

        // create an instance of ConfigLoader
        ConfigLoader configLoader = new ConfigLoader();

        // load config
        boolean configLoaded = configLoader.loadConfig(folder);
        if (!configLoaded) {
            logger.error("Invalid config file! Please see server console. Plugin has been disabled.");
            isLoadedCorrectly = false;
            return;
        }

        // create an instance of registerCustomCommands
        registerCustomCommands registerCustomCommands = new registerCustomCommands();
        Path commandsFolder = Path.of(folder + "/Commands/");


        // create the default command
        if (!registerCustomCommands.createDefaultCommand(commandsFolder)) {
            logger.error("Couldn't create default command file. Please see server console. Plugin has been disabled.");
            isLoadedCorrectly = false;
            return;
        }

        // load all custom commands
        if (!registerCustomCommands.loadCustomCommands(commandsFolder)) {
            logger.error("Invalid custom command(s)! Please see server console. Plugin has been disabled.");
            isLoadedCorrectly = false;
            return;
        }

        // append missing keys
        if (!registerCustomCommands.appendKeys(commandsFolder)) {
            logger.error("Couldn't append missing keys. Please see server console. Plugin has been disabled.");
            isLoadedCorrectly = false;
            return;
        }

        // logger.info("CustomCommands has been enabled. (" + BuildConstants.VERSION + ")");
    }

    // Initialization
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!isLoadedCorrectly) {
            return;
        }

        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("customcommands").aliases("cc").plugin(this).build();
        BrigadierCommand setMainCommand = MainCommand.createBrigadierCommand(proxy);

        commandManager.register(commandMeta, setMainCommand);
    }

    // register commands
    public static void registerCommand(String name, List<String> aliases, BrigadierCommand command) {
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(name)
                .aliases(aliases.stream().toArray(String[]::new))
                .plugin(CustomCommands.getInstance())  // Ensure you're passing the plugin instance
                .build();

        commandManager.register(commandMeta, command);
    }

    // cast message to player
    public static void print(CommandSource player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    // return instance
    public static CustomCommands getInstance() {
        return instance;
    }

    // return proxyServer
    public static ProxyServer getProxy() {
        return proxy;
    }
}
