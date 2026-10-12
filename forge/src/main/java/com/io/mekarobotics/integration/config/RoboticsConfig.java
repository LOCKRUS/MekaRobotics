package com.io.mekarobotics.integration.config;
import com.io.mekarobotics.Robotics;
import net.neoforged.fml.ModContainer;

public class RoboticsConfig {

    public static final GeneralConfig general = new GeneralConfig();
    public static final ClientConfig client = new ClientConfig();

    public static void registerConfigs(ModContainer container) {
        registerConfig(container, general);
        registerConfig(container, client);
    }

    private static void registerConfig(ModContainer container, IConfig config) {
        container.registerConfig(config.getConfigType(), config.getConfigSpec(), Robotics.MODNAME + "/" + config.getFileName() + ".toml");
    }

}
