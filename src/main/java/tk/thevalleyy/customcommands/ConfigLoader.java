package tk.thevalleyy.customcommands;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
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
                CustomCommands.logger.error(e.getMessage());
                return null;
            }
        }

        try {
            return new Toml().read(file);
        } catch (RuntimeException e) {
            CustomCommands.logger.error(e.getMessage());
            return null;
        }

    }

    // load config variables
    public boolean loadConfigVariables(Path folder) {
        Toml config = loadConfig(folder);
        if (config == null) {
            CustomCommands.logger.error("Failed to load the configuration file.");
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
