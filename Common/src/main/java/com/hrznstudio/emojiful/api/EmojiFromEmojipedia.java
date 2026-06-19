package com.hrznstudio.emojiful.api;

public class EmojiFromEmojipedia extends Emoji {
    @Override
    public String getUrl() {
        return "https://cdn.emojidex.com/emoji/px32/" + location + ".png";
    }
}
