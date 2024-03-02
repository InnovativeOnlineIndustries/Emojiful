package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.platform.services.IConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;


public class ForgeConfigHelper implements IConfigHelper {

    public static ModConfigSpec.BooleanValue renderEmoji;
    public static ModConfigSpec.BooleanValue showEmojiSelector;
    public static ModConfigSpec.BooleanValue showEmojiAutocomplete;
    public static ModConfigSpec.BooleanValue loadTwemoji;
    public static ModConfigSpec.BooleanValue loadCustom;
    public static ModConfigSpec.BooleanValue loadDatapack;
    public static ModConfigSpec.BooleanValue loadGifEmojis;

    public static ModConfigSpec.BooleanValue shortEmojiReplacement;

    public static ModConfigSpec.BooleanValue profanityFilter;
    public static ModConfigSpec.ConfigValue<String> profanityFilterReplacement;


    public static ModConfigSpec setup(ModConfigSpec.Builder builder) {
        builder.push("Emojiful").translation("emojiful.midnightconfig.title");
        renderEmoji = builder.comment("Enable Emoji Rendering").translation("emojiful.midnightconfig.enableRenderEmoji").define("enabled", true);
        showEmojiSelector = builder.comment("Enable Emoji Selection GUI in the chat text line").translation("emojiful.midnightconfig.showEmojiSelector").define("emoji_selector", true);
        showEmojiAutocomplete = builder.comment("Enable Emoji autocomplete in the chat text line").translation("emojiful.midnightconfig.emojiAutocomplete").define("emoji_autocomplete", true);
        loadGifEmojis = builder.comment("Load animated emojis, if disabled they will be a still image").translation("emojiful.midnightconfig.loadGifEmojis").define("gifs", true);
        shortEmojiReplacement = builder.comment("Replace short versions of emoji like :) into :smile: so they can be rendered as emoji").translation("emojiful.midnightconfig.shortEmojiReplacement").define("short_emoji_replacement", true);
        builder.push("EmojiTypes").translation("emojiful.midnightconfig.emojiTypes");
        loadTwemoji = builder.comment("Loads Twemojis used in sites like Twitter and Discord").translation("emojiful.midnightconfig.loadTwemoji").define("twemoji", false);
        loadCustom = builder.comment("Loads custom emojis provided by Emojiful").translation("emojiful.midnightconfig.enableLoadCustom").define("custom", true);
        loadDatapack = builder.comment("Loads datapack emojis provided by the server you join").translation("emojiful.midnightconfig.enableLoadDatapack").define("datapack", true);
        builder.pop();
        builder.pop();
        builder.push("ProfanityFilter").translation("emojiful.midnightconfig.profanityFilter");
        profanityFilter = builder.comment("Enable Profanity Filter, this will replace bad words with emoji").translation("emojiful.midnightconfig.enableProfanityFilter").define("enabled", false);
        profanityFilterReplacement = builder.comment("Replacement word for the profanity filter").translation("emojiful.midnightconfig.profanityFilterString").define("replacement", ":swear:");
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
