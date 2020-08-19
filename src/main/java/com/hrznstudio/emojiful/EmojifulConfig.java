package com.hrznstudio.emojiful;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Objects;

public class EmojifulConfig {
    private static EmojifulConfig instance;
    public ForgeConfigSpec.BooleanValue renderEmoji;
    public ForgeConfigSpec.BooleanValue profanityFilter;
    private final ForgeConfigSpec spec;

    private EmojifulConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Emojiful");
        renderEmoji = builder.comment("Enable Emoji Rendering").define("renderEmoji", true);
        profanityFilter = builder.comment("Enable Profanity Filter, this will replace bad words with the :swear: emoji").define("profanityFilter", true);
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
