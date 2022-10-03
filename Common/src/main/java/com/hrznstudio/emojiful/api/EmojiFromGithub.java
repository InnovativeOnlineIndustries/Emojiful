package com.hrznstudio.emojiful.api;

public class EmojiFromGithub extends Emoji {

    public String url;

    @Override
    public String getUrl() {
        return url;
    }


    @Override
    public String toString() {
        return "EmojiFromGithub{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", strings=" + strings +
                ", texts=" + texts +
                ", location='" + location + '\'' +
                ", version=" + version +
                ", sort=" + sort +
                ", worldBased=" + worldBased +
                ", deleteOldTexture=" + deleteOldTexture +
                ", img=" + img +
                ", frames=" + frames +
                ", finishedLoading=" + finishedLoading +
                ", loadedTextures=" + loadedTextures +
                '}';
    }
}
