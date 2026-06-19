package com.hrznstudio.emojiful.api;

public class EmojiFromTwitmoji extends Emoji {
    @Override
    public String getUrl() {
        return "https://raw.githubusercontent.com/iamcal/emoji-data/master/img-twitter-64/" + location;
    }
}
