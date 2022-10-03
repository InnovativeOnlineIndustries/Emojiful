package com.hrznstudio.emojiful.platform.services;

public interface IConfigHelper {

    boolean getProfanityFilter();
    String getReplacementString();
    boolean showEmojiAutocomplete();
    boolean showEmojiSelector();
    boolean renderEmoji();
    boolean loadGifEmojis();
    boolean shortEmojiReplacement();
    boolean loadTwemoji();
    boolean loadCustom();
    boolean loadDatapack();
}
