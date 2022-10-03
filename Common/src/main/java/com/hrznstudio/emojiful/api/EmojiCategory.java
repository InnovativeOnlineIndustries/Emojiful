package com.hrznstudio.emojiful.api;

public class EmojiCategory {

    private final String name;
    private final boolean worldBased;

    public EmojiCategory(String name, boolean worldBased) {
        this.name = name;
        this.worldBased = worldBased;
    }

    public String getName() {
        return name;
    }

    public boolean isWorldBased() {
        return worldBased;
    }
}
