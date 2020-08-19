package com.hrznstudio.emojiful;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Objects;

public class EmojifulConfig {
    private static EmojifulConfig instance;
    public ForgeConfigSpec.BooleanValue renderEmoji;
    private final ForgeConfigSpec spec;

    private EmojifulConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Emojiful");
        renderEmoji = builder.comment("Enable Emoji Rendering").define("renderEmoji", true);
        builder.pop();
        this.spec = builder.build();
    }

    public static ForgeConfigSpec init() {
        EmojifulConfig config = new EmojifulConfig(new ForgeConfigSpec.Builder());
        instance = config;
        return config.getSpec();
    }

    public static EmojifulConfig getInstance() {
        return Objects.requireNonNull(instance, "Called for Config before it's Initialization");
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }
}
