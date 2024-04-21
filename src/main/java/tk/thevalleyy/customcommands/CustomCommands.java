package tk.thevalleyy.customcommands;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@Plugin(
        id = "customcommands",
        name = "CustomCommands",
        version = BuildConstants.VERSION,
        description = "A plugin that allows you to create custom commands.",
        url = "https://github.com/thevalleyy/CustomCommands",
        authors = {"thevalleyy"}
)
public class CustomCommands {
    private final ProxyServer proxy;
    private final Logger logger;

    public Path folder = null;
    public static String Version = BuildConstants.VERSION;

    public static String Prefix;
    public static String NoPermission;
    public static String NoConsoleCommand;
    public static String CommandExecuted;
    public static String ConfigVersion;

    // loading config
    public Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return new Toml().read(file);
    }

    // load config variables
    public void loadConfigVariables() {
        Toml config = loadConfig(folder);
        if (config == null) {
            logger.error("Failed to load the configuration file.");
            return;
        }

        Prefix = config.getString("Prefix") + "<reset> ";
        NoPermission = config.getString("No-Permission");
        NoConsoleCommand = config.getString("No-Console-Command");
        CommandExecuted = config.getString("Command-Executed");
        ConfigVersion = String.valueOf(config.getDouble("Config-Version"));
    }

    // Construction
    @Inject
    public CustomCommands(ProxyServer proxy, Logger logger, @DataDirectory final Path folder) throws IOException {


        this.proxy = proxy;
        this.logger = logger;
        this.folder = folder;

        // load config
        Toml config = loadConfig(folder);
        if (config == null) {
            logger.error("config.toml is missing or invalid. Disabling plugin.");
            return;
        }

        // load config variables
        loadConfigVariables();

        // logger.info("CustomCommands has been enabled. (" + BuildConstants.VERSION + ")");
    }


    // Initialization
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("customcommands").aliases("cc").plugin(this).build();
        BrigadierCommand setMainCommand = MainCommand.createBrigadierCommand(proxy);

        commandManager.register(commandMeta, setMainCommand);
    }
}