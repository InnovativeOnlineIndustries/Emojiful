package com.hrznstudio.emojiful.api;

import com.hrznstudio.emojiful.Emojiful;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmojiFromGithub extends Emoji {

    public SimpleTexture img;
    public ResourceLocation resourceLocation = loading_texture;
    public String url;

    @Override
    public void checkLoad() {
        if (img != null)
            return;

        img = new DownloadImageData(new File("emojiful/cache/" + name + "-" + version), url, loading_texture);
        resourceLocation = new ResourceLocation(Emojiful.MODID, "texures/emoji/" + location.toLowerCase().replaceAll("[^a-z0-9/._-]", "") + "_" + version);
        Minecraft.getInstance().getTextureManager().loadTexture(resourceLocation, img);
    }

    public ResourceLocation getResourceLocationForBinding() {
        checkLoad();
        if (deleteOldTexture) {
            img.deleteGlTexture();
            deleteOldTexture = false;
        }
        return resourceLocation;
    }

    @Override
    public boolean test(String s) {
        for (String text : strings)
            if (s.equalsIgnoreCase(text))
                return true;
        return false;
    }

    public class DownloadImageData extends SimpleTexture {
        private final File cacheFile;
        private final String imageUrl;
        private NativeImage nativeImage;
        private Thread imageThread;
        private boolean textureUploaded;

        public DownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation) {
            super(textureResourceLocation);
            this.cacheFile = cacheFileIn;
            this.imageUrl = imageUrlIn;
        }

        private void checkTextureUploaded() {
            if (!this.textureUploaded) {
                if (this.nativeImage != null) {
                    if (this.textureLocation != null) {
                        this.deleteGlTexture();
                    }
                    TextureUtil.prepareImage(super.getGlTextureId(), this.nativeImage.getWidth(), this.nativeImage.getHeight());
                    this.nativeImage.uploadTextureSub(0, 0, 0, true);
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
            TextureUtil.prepareImage(this.getGlTextureId(), imageIn.getWidth(), imageIn.getHeight());
            imageIn.uploadTextureSub(0, 0, 0, true);
        }

        @Nullable
        private NativeImage loadTexture(InputStream inputStreamIn) {
            NativeImage nativeimage = null;

            try {
                nativeimage = NativeImage.read(inputStreamIn);
            } catch (IOException ioexception) {
                Emojiful.LOGGER.warn("Error while loading the skin texture", (Throwable)ioexception);
            }

            return nativeimage;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException {
            if (this.imageThread == null) {
                if (this.cacheFile != null && this.cacheFile.isFile()) {
                    try {
                        FileInputStream fileinputstream = new FileInputStream(this.cacheFile);
                        setImage(this.loadTexture(fileinputstream));
                    } catch (IOException ioexception) {
                        this.loadTextureFromServer();
                    }
                } else {
                    this.loadTextureFromServer();
                }
            }
        }

        protected void loadTextureFromServer() {
            this.imageThread = new Thread("Emojiful Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
                @Override
                public void run() {
                    HttpURLConnection httpurlconnection = null;

                    try {
                        httpurlconnection = (HttpURLConnection) (new URL(DownloadImageData.this.imageUrl)).openConnection(Minecraft.getInstance().getProxy());
                        httpurlconnection.setDoInput(true);
                        httpurlconnection.setDoOutput(false);
                        httpurlconnection.connect();
                        if (httpurlconnection.getResponseCode() / 100 == 2) {
                            int contentLength = httpurlconnection.getContentLength();
                            InputStream inputStream;

                            if (DownloadImageData.this.cacheFile != null) {
                                FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), DownloadImageData.this.cacheFile);
                                inputStream = new FileInputStream(DownloadImageData.this.cacheFile);
                            } else {
                                inputStream = httpurlconnection.getInputStream();
                            }

                            DownloadImageData.this.setImage(DownloadImageData.this.loadTexture(inputStream));
                        } else {
                            EmojiFromGithub.this.resourceLocation = noSignal_texture;
                            EmojiFromGithub.this.deleteOldTexture = true;
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        EmojiFromGithub.this.resourceLocation = error_texture;
                        EmojiFromGithub.this.deleteOldTexture = true;

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
    }
}
