package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.platform.services.IConfigHelper;
import eu.midnightdust.lib.config.MidnightConfig;

public class FabricConfigHelper extends MidnightConfig implements IConfigHelper {

    @Comment(centered = true)
    public static Comment title;

    @Comment
    public static Comment enableRenderEmoji;
    @Entry
    public static boolean renderEmoji = true;

    @Comment
    public static Comment showEmojiSelector;
    @Entry
    public static boolean emojiSelector = true;

    @Comment
    public static Comment showEmojiAutocomplete;
    @Entry
    public static boolean emojiAutocomplete = true;

    @Comment
    public static Comment loadGifEmojis;
    @Entry
    public static boolean gifEmojis = true;

    @Comment
    public static Comment shortEmojiReplacement;
    @Entry
    public static boolean emojiReplacement = true;

    @Comment(centered = true)
    public static Comment emojiTypes;

    @Comment
    public static Comment loadTwemoji;
    @Entry
    public static boolean enableLoadTwemoji = false;

    @Comment
    public static Comment enableLoadCustom;
    @Entry
    public static boolean loadCustom = true;

    @Comment
    public static Comment enableLoadDatapack;
    @Entry
    public static boolean loadDatapack = true;

    @Comment
    public static Comment enableProfanityFilter;
    @Entry
    public static boolean profanityFilter = false;

    @Comment
    public static Comment profanityFilterString;
    @Entry
    public static String profanityFilterReplacement = ":swear:";


    @Override
    public boolean getProfanityFilter() {
        return profanityFilter;
    }

    @Override
    public String getReplacementString() {
        return profanityFilterReplacement;
    }

    @Override
    public boolean loadGifEmojis() {
        return gifEmojis;
    }

    @Override
    public boolean shortEmojiReplacement() {
        return emojiReplacement;
    }

    @Override
    public boolean loadTwemoji() {
        return enableLoadTwemoji;
    }

    @Override
    public boolean loadCustom() {
        return loadCustom;
    }

    @Override
    public boolean loadDatapack() {
        return loadDatapack;
    }

    @Override
    public boolean renderEmoji() {
        return renderEmoji;
    }

    @Override
    public boolean showEmojiAutocomplete() {
        return emojiAutocomplete;
    }

    @Override
    public boolean showEmojiSelector() {
        return emojiSelector;
    }
}

