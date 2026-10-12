package com.io.mekarobotics.integration.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;

public interface IConfig {

    void load(ModConfigSpec.Builder builder);

    String getFileName();

    ModConfigSpec getConfigSpec();

    ModConfig.Type getConfigType();
}
