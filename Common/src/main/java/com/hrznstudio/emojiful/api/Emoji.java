package com.hrznstudio.emojiful.api;

import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.util.EmojiUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Emoji implements Predicate<String> {
    public static final ResourceLocation loading_texture = new ResourceLocation(Constants.MOD_ID, "textures/gui/26a0.png");
    public static final ResourceLocation noSignal_texture = new ResourceLocation(Constants.MOD_ID, "textures/gui/26d4.png");
    public static final ResourceLocation error_texture = new ResourceLocation(Constants.MOD_ID, "textures/gui/26d4.png");

    public static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    public static final AtomicInteger threadFileLoaderCounter = new AtomicInteger(0);
    public String name;
    public List<String> strings = new ArrayList<>();
    public List<String> texts = new ArrayList<>();
    public String location;
    public int version = 1;
    public int sort = 0;
    public boolean worldBased = false;
    public boolean deleteOldTexture;
    public List<DownloadImageData> img = new ArrayList<>();
    public List<ResourceLocation> frames = new ArrayList<>();
    public boolean finishedLoading = false;
    public boolean loadedTextures = false;
    private String shortString;
    private String regex;
    private Pattern regexPattern;
    private String textRegex;
    private Thread imageThread;
    private Thread gifLoaderThread;

    public void checkLoad() {
        if (imageThread == null && !finishedLoading) {
            loadImage();
        } else if (!loadedTextures) {
            loadedTextures = true;
        }

    }

    public ResourceLocation getResourceLocationForBinding() {
        checkLoad();
        if (deleteOldTexture) {
            img.forEach(AbstractTexture::releaseId);
            deleteOldTexture = false;
        }
        return finishedLoading && frames.size() > 0 ? frames.get((int) (System.currentTimeMillis() / 10D % frames.size())) : loading_texture;
    }

    @Override
    public boolean test(String s) {
        for (String text : strings)
            if (s.equalsIgnoreCase(text))
                return true;
        return false;
    }

    public boolean worldBased() {
        return worldBased;
    }

    public String getShorterString() {
        if (shortString != null) return shortString;
        shortString = strings.get(0);
        for (String string : strings) {
            if (string.length() < shortString.length()) {
                shortString = string;
            }
        }
        return shortString;
    }

    public Pattern getRegex() {
        if (regexPattern != null) return regexPattern;
        regexPattern = Pattern.compile(getRegexString());
        return regexPattern;
    }

    public String getRegexString() {
        if (regex != null) return regex;
        List<String> processed = new ArrayList<>();
        for (String string : strings) {
            char last = string.toLowerCase().charAt(string.length() - 1);
            String s = string;
            if (last >= 97 && last <= 122) {
                s = string + "\\b";
            }
            char first = string.toLowerCase().charAt(0);
            if (first >= 97 && first <= 122) {
                s = "\\b" + s;
            }
            processed.add(EmojiUtil.cleanStringForRegex(s));
        }
        regex = String.join("|", processed);
        return regex;
    }

    public String getTextRegex() {
        if (textRegex != null) return textRegex;
        List<String> processed = new ArrayList<>();
        for (String string : texts) {
            processed.add(EmojiUtil.cleanStringForRegex(string));
        }

        // (?<=^|\s) ensures the character before the text is either the start of the string or a whitespace
        // (?=$|\s) ensures the character after the text is either the end of the string or a whitespace
        textRegex = "(?<=^|\\s)(" + String.join("|", processed) + ")(?=$|\\s)";
        return textRegex;
    }

    private void loadImage() {
        File cache = getCache();
        if (cache.exists()) {
            if (getUrl().endsWith(".gif") && Services.CONFIG.loadGifEmojis()) {
                if (gifLoaderThread == null) {
                    gifLoaderThread = new Thread("Emojiful Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
                        @Override
                        public void run() {
                            try {
                                loadTextureFrames(EmojiUtil.splitGif(cache));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    this.gifLoaderThread.setDaemon(true);
                    this.gifLoaderThread.start();
                }
            } else {
                try {
                    DownloadImageData imageData = new DownloadImageData(ImageIO.read(cache), loading_texture);
                    ResourceLocation resourceLocation = new ResourceLocation(Constants.MOD_ID, "texures/emoji/" + name.toLowerCase().replaceAll("[^a-z0-9/._-]", "") + "_" + version);
                    Minecraft.getInstance().getTextureManager().register(resourceLocation, imageData);
                    img.add(imageData);
                    frames.add(resourceLocation);
                    this.finishedLoading = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (this.imageThread == null) {
            loadTextureFromServer();
        }
    }

    public String getUrl() {
        return "https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/1.20-plus/" + location;
    }

    public File getCache() {
        return new File("emojiful/cache/" + name + "-" + version);
    }

    public void loadTextureFrames(List<Pair<BufferedImage, Integer>> framesPair) {
        Minecraft.getInstance().executeBlocking(() -> {
            int i = 0;
            for (Pair<BufferedImage, Integer> bufferedImage : framesPair) {
                DownloadImageData imageData = new DownloadImageData(bufferedImage.getKey(), loading_texture);
                ResourceLocation resourceLocation = new ResourceLocation(Constants.MOD_ID, "texures/emoji/" + name.toLowerCase().replaceAll("[^a-z0-9/._-]", "") + "_" + version + "_frame" + i);
                Minecraft.getInstance().getTextureManager().register(resourceLocation, imageData);
                img.add(imageData);
                for (Integer integer = 0; integer < bufferedImage.getValue(); integer++) {
                    frames.add(resourceLocation);
                }
                ++i;
            }
            Emoji.this.finishedLoading = true;
        });
    }

    protected void loadTextureFromServer() {
        this.imageThread = new Thread("Emojiful Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
            @Override
            public void run() {
                HttpURLConnection httpurlconnection = null;
                try {
                    httpurlconnection = (HttpURLConnection) (new URL(getUrl()).openConnection(Minecraft.getInstance().getProxy()));
                    httpurlconnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                    httpurlconnection.setDoInput(true);
                    httpurlconnection.setDoOutput(false);
                    httpurlconnection.connect();
                    if (httpurlconnection.getResponseCode() / 100 == 2) {
                        if (getCache() != null) {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), getCache());
                        }
                        Emoji.this.finishedLoading = true;
                        loadImage();
                    } else {
                        Emoji.this.frames = new ArrayList<>();
                        Emoji.this.frames.add(noSignal_texture);
                        Emoji.this.deleteOldTexture = true;
                        Emoji.this.finishedLoading = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Emoji.this.frames = new ArrayList<>();
                    Emoji.this.frames.add(error_texture);
                    Emoji.this.deleteOldTexture = true;
                    Emoji.this.finishedLoading = true;
                } finally {
                    if (httpurlconnection != null) {
                        httpurlconnection.disconnect();
                    }
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }

    public class DownloadImageData extends SimpleTexture {
        private final BufferedImage cacheFile;
        public boolean textureUploaded;
        private NativeImage nativeImage;

        public DownloadImageData(BufferedImage cacheFileIn, ResourceLocation textureResourceLocation) {
            super(textureResourceLocation);
            this.cacheFile = cacheFileIn;
        }

        private void checkTextureUploaded() {
            if (!this.textureUploaded) {
                if (this.nativeImage != null) {
                    if (this.location != null) {
                        this.releaseId();
                    }
                    TextureUtil.prepareImage(super.getId(), this.nativeImage.getWidth(), this.nativeImage.getHeight());
                    this.nativeImage.upload(0, 0, 0, true);
                    this.textureUploaded = true;
                }
            }
        }

        private void setImage(NativeImage nativeImageIn) {
            Minecraft.getInstance().execute(() -> {
                this.textureUploaded = true;
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> {
                        this.upload(nativeImageIn);
                    });
                } else {
                    this.upload(nativeImageIn);
                }

            });
        }

        private void upload(NativeImage imageIn) {
            TextureUtil.prepareImage(this.getId(), imageIn.getWidth(), imageIn.getHeight());
            imageIn.upload(0, 0, 0, true);
        }

        @Nullable
        private NativeImage loadTexture(InputStream inputStreamIn) {
            NativeImage nativeimage = null;
            try {
                nativeimage = NativeImage.read(inputStreamIn);
            } catch (IOException ioexception) {
                Constants.LOG.warn("Error while loading the skin texture", ioexception);
            }
            return nativeimage;
        }

        @Override
        public void load(ResourceManager resourceManager) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(this.cacheFile, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            setImage(this.loadTexture(is));
        }

    }
}
