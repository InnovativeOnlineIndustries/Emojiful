package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.platform.services.IConfigHelper;

public class FabricConfigHelper implements IConfigHelper {

    //Todo: Code in Fabric Lib

    @Override
    public boolean getProfanityFilter() {
        return false;
    }

    @Override
    public String getReplacementString() {
        return null;
    }

    @Override
    public boolean loadGifEmojis() {
        return false;
    }

    @Override
    public boolean shortEmojiReplacement() {
        return false;
    }

    @Override
    public boolean loadTwemoji() {
        return false;
    }

    @Override
    public boolean loadCustom() {
        return false;
    }

    @Override
    public boolean loadDatapack() {
        return false;
    }

    @Override
    public boolean renderEmoji() {
        return false;
    }

    @Override
    public boolean showEmojiAutocomplete() {
        return false;
    }

    @Override
    public boolean showEmojiSelector() {
        return false;
    }
}

