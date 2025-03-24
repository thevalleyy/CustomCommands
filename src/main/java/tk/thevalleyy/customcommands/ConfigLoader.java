package tk.thevalleyy.customcommands;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
    Toml config;

    public boolean loadConfig(Path folder) {
        File file = new File(folder.toFile(), "config.toml"); // file object (config.toml)

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // create plugin folder
        }

        if (!file.exists() || file.length() == 0) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) { // get the default config file
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    CustomCommands.logger.error("Failed to read the default config from the jar file. Report this to the plugin developer.");
                    return false;
                }
            } catch (IOException e) {
                CustomCommands.logger.error(e.getMessage());
                return false;
            }
        }

        try {
            config = new Toml().read(file);
        } catch (RuntimeException e) {
            CustomCommands.logger.error(e.getMessage());
            return false;
        }

        if (config == null || config.isEmpty()) {
            CustomCommands.logger.error("Failed to load the configuration file."); // fallback
            return false;
        }

        CustomCommands.Prefix = config.getString("Prefix") + "<reset> ";
        CustomCommands.NoPermission = config.getString("No-Permission");
        CustomCommands.NoConsoleCommand = config.getString("No-Console-Command");
        CustomCommands.CommandExecuted = config.getString("Command-Executed");
        CustomCommands.ConfigVersion = String.valueOf(config.getDouble("Config-Version"));

        return true;
    }
}
