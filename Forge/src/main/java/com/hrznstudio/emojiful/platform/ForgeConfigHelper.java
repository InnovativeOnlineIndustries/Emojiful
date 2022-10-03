package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.platform.services.IConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;


public class ForgeConfigHelper implements IConfigHelper {

    public static ForgeConfigSpec.BooleanValue renderEmoji;
    public static ForgeConfigSpec.BooleanValue showEmojiSelector;
    public static ForgeConfigSpec.BooleanValue showEmojiAutocomplete;
    public static ForgeConfigSpec.BooleanValue loadTwemoji;
    public static ForgeConfigSpec.BooleanValue loadCustom;
    public static ForgeConfigSpec.BooleanValue loadDatapack;
    public static ForgeConfigSpec.BooleanValue loadGifEmojis;

    public static ForgeConfigSpec.BooleanValue shortEmojiReplacement;

    public static ForgeConfigSpec.BooleanValue profanityFilter;
    public static ForgeConfigSpec.ConfigValue<String> profanityFilterReplacement;


    public static ForgeConfigSpec setup(ForgeConfigSpec.Builder builder){
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
        return builder.build();
    }

    @Override
    public boolean getProfanityFilter() {
        return profanityFilter.get();
    }

    @Override
    public String getReplacementString() {
        return profanityFilterReplacement.get();
    }

    @Override
    public boolean loadGifEmojis() {
        return loadGifEmojis.get();
    }

    @Override
    public boolean showEmojiAutocomplete() {
        return showEmojiAutocomplete.get();
    }

    @Override
    public boolean showEmojiSelector() {
        return showEmojiSelector.get();
    }

    @Override
    public boolean renderEmoji() {
        return renderEmoji.get();
    }

    @Override
    public boolean shortEmojiReplacement() {
        return shortEmojiReplacement.get();
    }

    @Override
    public boolean loadTwemoji() {
        return loadTwemoji.get();
    }

    @Override
    public boolean loadCustom() {
        return loadCustom.get();
    }

    @Override
    public boolean loadDatapack() {
        return loadDatapack.get();
    }
}
