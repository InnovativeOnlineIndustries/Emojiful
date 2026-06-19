package com.hrznstudio.emojiful.api;

import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.util.EmojiUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Emoji implements Predicate<String> {
    public static final Identifier loading_texture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/26a0.png");
    public static final Identifier noSignal_texture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/26d4.png");
    public static final Identifier error_texture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/26d4.png");

    public static final AtomicInteger threadDownloadCounter = new AtomicInteger();
    public String name;
    public List<String> strings = new ArrayList<>();
    public List<String> texts = new ArrayList<>();
    public String location;
    public int version = 1;
    public int sort;
    public boolean worldBased;
    public boolean deleteOldTexture;
    public List<DynamicTexture> img = new ArrayList<>();
    public List<Identifier> frames = new ArrayList<>();
    public boolean finishedLoading;
    public boolean loadedTextures;
    private String shortString;
    private String regex;
    private Pattern regexPattern;
    private String textRegex;
    private Thread imageThread;
    private Thread gifLoaderThread;

    public void checkLoad() {
        if (imageThread == null && !finishedLoading) {
            loadImage();
        }
    }

    public Identifier getResourceLocationForBinding() {
        checkLoad();
        return finishedLoading && !frames.isEmpty()
                ? frames.get((int) (System.currentTimeMillis() / 10D % frames.size()))
                : loading_texture;
    }

    @Override
    public boolean test(String value) {
        return strings.stream().anyMatch(text -> value.equalsIgnoreCase(text));
    }

    public boolean worldBased() {
        return worldBased;
    }

    public String getShorterString() {
        if (shortString == null) {
            shortString = strings.stream().min(java.util.Comparator.comparingInt(String::length)).orElse("");
        }
        return shortString;
    }

    public Pattern getRegex() {
        if (regexPattern == null) {
            regexPattern = Pattern.compile(getRegexString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
        return regexPattern;
    }

    public String getRegexString() {
        if (regex != null) {
            return regex;
        }
        List<String> processed = new ArrayList<>();
        for (String string : strings) {
            String value = string;
            char last = string.toLowerCase().charAt(string.length() - 1);
            if (last >= 'a' && last <= 'z') {
                value += "\\b";
            }
            char first = string.toLowerCase().charAt(0);
            if (first >= 'a' && first <= 'z') {
                value = "\\b" + value;
            }
            processed.add(EmojiUtil.cleanStringForRegex(value));
        }
        regex = String.join("|", processed);
        return regex;
    }

    public String getTextRegex() {
        if (textRegex == null) {
            textRegex = "(?<=^|\\s)("
                    + String.join("|", texts.stream().map(EmojiUtil::cleanStringForRegex).toList())
                    + ")(?=$|\\s)";
        }
        return textRegex;
    }

    private void loadImage() {
        File cache = getCache();
        if (cache.isFile()) {
            if (getUrl().endsWith(".gif") && Services.CONFIG.loadGifEmojis()) {
                if (gifLoaderThread == null) {
                    gifLoaderThread = new Thread(() -> {
                        try {
                            loadTextureFrames(EmojiUtil.splitGif(cache));
                        } catch (IOException exception) {
                            markFailed(error_texture, exception);
                        }
                    }, "Emojiful GIF Loader #" + threadDownloadCounter.incrementAndGet());
                    gifLoaderThread.setDaemon(true);
                    gifLoaderThread.start();
                }
            } else {
                try {
                    registerFrames(List.of(Pair.of(javax.imageio.ImageIO.read(cache), 1)));
                } catch (IOException exception) {
                    markFailed(error_texture, exception);
                }
            }
        } else if (imageThread == null) {
            loadTextureFromServer();
        }
    }

    public String getUrl() {
        return "https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/1.20-plus/" + location;
    }

    public File getCache() {
        return new File("emojiful/cache/" + name + "-" + version);
    }

    public void loadTextureFrames(List<Pair<BufferedImage, Integer>> frameImages) {
        registerFrames(frameImages);
    }

    private void registerFrames(List<Pair<BufferedImage, Integer>> frameImages) {
        Minecraft.getInstance().executeBlocking(() -> {
            int index = 0;
            for (Pair<BufferedImage, Integer> frame : frameImages) {
                BufferedImage image = frame.getLeft();
                if (image == null) {
                    continue;
                }
                NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        nativeImage.setPixel(x, y, image.getRGB(x, y));
                    }
                }
                DynamicTexture texture = new DynamicTexture(() -> "Emojiful " + name, nativeImage);
                Identifier identifier = Identifier.fromNamespaceAndPath(
                        Constants.MOD_ID,
                        "textures/emoji/" + safeName() + "_" + version + "_frame_" + index++
                );
                Minecraft.getInstance().getTextureManager().register(identifier, texture);
                img.add(texture);
                for (int tick = 0; tick < Math.max(1, frame.getRight()); tick++) {
                    frames.add(identifier);
                }
            }
            finishedLoading = true;
        });
    }

    private String safeName() {
        return name.toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
    }

    protected void loadTextureFromServer() {
        imageThread = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                File cache = getCache();
                File parent = cache.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                connection = (HttpURLConnection) new URL(getUrl()).openConnection(Minecraft.getInstance().getProxy());
                connection.setRequestProperty("User-Agent", "Emojiful/" + Constants.MOD_NAME);
                connection.setDoInput(true);
                connection.connect();
                if (connection.getResponseCode() / 100 == 2) {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), cache);
                    loadImage();
                } else {
                    markFailed(noSignal_texture, null);
                }
            } catch (Exception exception) {
                markFailed(error_texture, exception);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, "Emojiful Texture Downloader #" + threadDownloadCounter.incrementAndGet());
        imageThread.setDaemon(true);
        imageThread.start();
    }

    private void markFailed(Identifier fallback, Exception exception) {
        if (exception != null) {
            Constants.LOG.warn("Could not load emoji {}", name, exception);
        }
        frames = new ArrayList<>(List.of(fallback));
        deleteOldTexture = false;
        finishedLoading = true;
    }
}
