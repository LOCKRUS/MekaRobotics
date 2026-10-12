package com.io.mekarobotics.integration.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public abstract class BaseConfig implements IConfig {

    private ModConfigSpec spec;

    public BaseConfig() {
        load(new ModConfigSpec.Builder());
    }

    public abstract ModConfigSpec define(ModConfigSpec.Builder builder);

    @Override
    public void load(ModConfigSpec.Builder builder) {
        spec = define(builder);
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return spec;
    }
}
