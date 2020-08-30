package com.hrznstudio.emojiful;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Objects;

public class EmojifulConfig {

    private static EmojifulConfig instance;

    public ForgeConfigSpec.BooleanValue renderEmoji;
    public ForgeConfigSpec.BooleanValue showEmojiSelector;
    public ForgeConfigSpec.BooleanValue showEmojiAutocomplete;
    public ForgeConfigSpec.BooleanValue loadTwemoji;
    public ForgeConfigSpec.BooleanValue loadCustom;
    public ForgeConfigSpec.BooleanValue loadDatapack;
    public ForgeConfigSpec.BooleanValue loadGifEmojis;
    public ForgeConfigSpec.BooleanValue shortEmojiReplacement;

    public ForgeConfigSpec.BooleanValue profanityFilter;
    public ForgeConfigSpec.ConfigValue<String> profanityFilterReplacement;

    private final ForgeConfigSpec spec;

    private EmojifulConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Emojiful");
        renderEmoji = builder.comment("Enable Emoji Rendering").define("enabled", true);
        showEmojiSelector = builder.comment("Enable Emoji Selection GUI in the chat text line").define("emoji_selector", true);
        showEmojiAutocomplete = builder.comment("Enable Emoji autocomplete in the chat text line").define("emoji_autocomplete", true);
        loadGifEmojis = builder.comment("Load animated emojis, if disabled they will be a still image").define("gifs", true);
        shortEmojiReplacement = builder.comment("Replace short versions of emoji like :) into :smile: so they can be rendered as emoji").define("short_emoji_replacement", true);
        builder.push("EmojiTypes");
        loadTwemoji = builder.comment("Loads Twemojis used in sites like Twitter and Discord").define("twemoji", true);
        loadCustom = builder.comment("Loads custom emojis provided by Emojiful").define("custom", true);
        loadDatapack = builder.comment("Loads datapack emojis provided by the server you join").define("datapack", true);
        builder.pop();
        builder.pop();
        builder.push("ProfanityFilter");
        profanityFilter = builder.comment("Enable Profanity Filter, this will replace bad words with emoji").define("enabled", false);
        profanityFilterReplacement = builder.comment("Replacement word for the profanity filter").define("replacement", ":swear:");
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
