package com.hrznstudio.emojiful.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.hrznstudio.emojiful.Emojiful;
import com.hrznstudio.emojiful.EmojifulConfig;
import com.hrznstudio.emojiful.api.Emoji;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.EmptyGlyph;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.IGlyph;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Unit;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static net.minecraft.util.text.TextProcessing.func_238339_a_;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class EmojiFontRenderer extends FontRenderer {

    //<+(\w)+:+(\w)+>

    public static LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> RECENT_STRINGS = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Pair<String, HashMap<Integer, Emoji>>>() {
        @Override
        public Pair<String, HashMap<Integer, Emoji>> load(String key) throws Exception {
            return getEmojiFormattedString(key);
        }
    });


    public EmojiFontRenderer(Minecraft minecraft, FontRenderer fontRenderer) {
        super(fontRenderer.font);
    }

    private TextureAtlasSprite sprite;

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public int getStringWidth(String text) {
        if (text != null) {
            try {
                text = RECENT_STRINGS.get(text.replaceAll("Buuz135", "Buuz135 :blobcatbolb: ")).getKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.getStringWidth(text);
    }

    @Override
    public int func_238414_a_(ITextProperties textProperties) {
        return this.getStringWidth(textProperties.getString());
    }

    public static Pair<String, HashMap<Integer, Emoji>> getEmojiFormattedString(String text) {
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        if (EmojifulConfig.getInstance().renderEmoji.get() && !StringUtil.isNullOrEmpty(text)) {
            String unformattedText = TextFormatting.getTextWithoutFormattingCodes(text);
            if (StringUtil.isNullOrEmpty(unformattedText))
                return Pair.of(text, emojis);
            String[] split = unformattedText.split(" ");
            List<Pair<Emoji, String>> addedEmojis = new ArrayList<>();
            for (String word : split) {
                Emoji wordEmoji = null;
                for (Emoji emoji : Emojiful.EMOJI_LIST) {
                    if (emoji.test(word.trim())) {
                        wordEmoji = emoji;
                        break;
                    }
                }
                if (wordEmoji != null) {
                    addedEmojis.add(Pair.of(wordEmoji, word));
                }
            }
            String fomattingText = text.toLowerCase(Locale.ENGLISH);
            for (Pair<Emoji, String> entry : addedEmojis) {
                String emojiText = entry.getValue().toLowerCase(Locale.ENGLISH);
                int index = fomattingText.indexOf(emojiText);
                emojis.put(index, entry.getKey());
                fomattingText = fomattingText.replaceFirst(Pattern.quote(emojiText), "\u2603");
                text = text.replaceFirst("(?i)" + Pattern.quote(emojiText), "\u2603");
            }
        }
        return Pair.of(text, emojis);
    }

    @Override
    public int renderString(String p_228079_1_, float p_228079_2_, float p_228079_3_, int p_228079_4_, boolean p_228079_5_, Matrix4f p_228079_6_, IRenderTypeBuffer p_228079_7_, boolean p_228079_8_, int p_228079_9_, int p_228079_10_) {
        return super.renderString(p_228079_1_, p_228079_2_, p_228079_3_, p_228079_4_, p_228079_5_, p_228079_6_, p_228079_7_, p_228079_8_, p_228079_9_, p_228079_10_);
    }

    @Override
    protected float renderStringAtPos(String text, float x, float y, int color, boolean isShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
        if (text.isEmpty())
            return 0;
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        try {
            Pair<String, HashMap<Integer, Emoji>> cache = RECENT_STRINGS.get(text.replaceAll("Buuz135", "Buuz135 :blobcatbolb: "));
            text = cache.getLeft();
            emojis = cache.getRight();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, isShadow, matrix, isTransparent, packedLight);
        TextProcessing.func_238346_c_(text, Style.EMPTY, fontrenderer$characterrenderer);
        return fontrenderer$characterrenderer.func_238441_a_(colorBackgroundIn, x);
    }

    @Override
    protected float func_238426_c_(ITextProperties textProperties, float x, float y, int color, boolean isShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
        final HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        EmojiCharacterRenderer fontrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, isShadow, matrix, isTransparent, packedLight);
        textProperties.func_230439_a_((p_238337_1_, text) -> {
            try {
                Pair<String, HashMap<Integer, Emoji>> cache = RECENT_STRINGS.get(text.replaceAll("Buuz135", "Buuz135 :blobcatbolb: "));
                text = cache.getLeft();
                cache.getRight().forEach(emojis::put);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Optional<?> optional = func_238339_a_(text, 0, p_238337_1_, fontrenderer) ? Optional.empty() : Optional.of(Unit.INSTANCE);
            emojis.clear();
            return optional;
        }, Style.EMPTY).isPresent();
        return fontrenderer.func_238441_a_(colorBackgroundIn, x);
    }

    //protected float func_238426_c_(ITextProperties textProperties, float x, float y, int color, boolean isShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {


    //        float f = isShadow ? 0.25F : 1.0F;
    //        float red = (float)(color >> 16 & 255) / 255.0F * f;
    //        float green = (float)(color >> 8 & 255) / 255.0F * f;
    //        float blue = (float)(color & 255) / 255.0F * f;
    //        float f4 = x;
    //        float f5 = red;
    //        float f6 = green;
    //        float f7 = blue;
    //        float f8 = (float)(color >> 24 & 255) / 255.0F;
    //        boolean obfuscated = false;
    //        boolean bold = false;
    //        boolean italic = false;
    //        boolean underline = false;
    //        boolean strike = false;
    //        List<TexturedGlyph.Effect> list = Lists.newArrayList();
    //        text = getEmojiFormattedString(text);
    //        for(int i = 0; i < text.length(); ++i) {
    //            char charToRender = text.charAt(i);
    //            if (charToRender == 167 && i + 1 < text.length()) {
    //                TextFormatting textformatting = TextFormatting.fromFormattingCode(text.charAt(i + 1));
    //                if (textformatting != null) {
    //                    if (textformatting.isNormalStyle()) {
    //                        obfuscated = false;
    //                        bold = false;
    //                        strike = false;
    //                        underline = false;
    //                        italic = false;
    //                        f5 = red;
    //                        f6 = green;
    //                        f7 = blue;
    //                    }
    //
    //                    if (textformatting.getColor() != null) {
    //                        int j = textformatting.getColor();
    //                        f5 = (float) (j >> 16 & 255) / 255.0F * f;
    //                        f6 = (float) (j >> 8 & 255) / 255.0F * f;
    //                        f7 = (float) (j & 255) / 255.0F * f;
    //                    } else if (textformatting == TextFormatting.OBFUSCATED) {
    //                        obfuscated = true;
    //                    } else if (textformatting == TextFormatting.BOLD) {
    //                        bold = true;
    //                    } else if (textformatting == TextFormatting.STRIKETHROUGH) {
    //                        strike = true;
    //                    } else if (textformatting == TextFormatting.UNDERLINE) {
    //                        underline = true;
    //                    } else if (textformatting == TextFormatting.ITALIC) {
    //                        italic = true;
    //                    }
    //                }
    //
    //                ++i;
    //            } else {
    //                if (EmojifulConfig.getInstance().renderEmoji.get() && this.emojis.get(i) != null){
    //                    Emoji emoji = this.emojis.get(i);
    //                    if (emoji != null) {
    //                        f4 += this.renderEmoji(emoji,  f4, y, matrix, buffer);
    //                    }
    //                } else {
    //                    IGlyph iglyph = this.font.findGlyph(charToRender);
    //                    TexturedGlyph texturedglyph = obfuscated && charToRender != ' ' ? this.font.obfuscate(iglyph) : this.font.getGlyph(charToRender);
    //                    if (!(texturedglyph instanceof EmptyGlyph)) {
    //                        float f9 = bold ? iglyph.getBoldOffset() : 0.0F;
    //                        float f10 = isShadow ? iglyph.getShadowOffset() : 0.0F;
    //                        IVertexBuilder ivertexbuilder = buffer.getBuffer(texturedglyph.getRenderType(isTransparent));
    //                        this.drawGlyph(texturedglyph, bold, italic, f9, f4 + f10, y + f10, matrix, ivertexbuilder, f5, f6, f7, f8, packedLight);
    //                    }
    //
    //                    float f15 = iglyph.getAdvance(bold);
    //                    float f16 = isShadow ? 1.0F : 0.0F;
    //                    if (strike) {
    //                        list.add(new TexturedGlyph.Effect(f4 + f16 - 1.0F, y + f16 + 4.5F, f4 + f16 + f15, y + f16 + 4.5F - 1.0F, -0.01F, f5, f6, f7, f8));
    //                    }
    //
    //                    if (underline) {
    //                        list.add(new TexturedGlyph.Effect(f4 + f16 - 1.0F, y + f16 + 9.0F, f4 + f16 + f15, y + f16 + 9.0F - 1.0F, -0.01F, f5, f6, f7, f8));
    //                    }
    //                    f4 += f15;
    //                }
    //            }
    //        }
    //        if (colorBackgroundIn != 0) {
    //            float f11 = (float)(colorBackgroundIn >> 24 & 255) / 255.0F;
    //            float f12 = (float)(colorBackgroundIn >> 16 & 255) / 255.0F;
    //            float f13 = (float)(colorBackgroundIn >> 8 & 255) / 255.0F;
    //            float f14 = (float)(colorBackgroundIn & 255) / 255.0F;
    //            list.add(new TexturedGlyph.Effect(x - 1.0F, y + 9.0F, f4 + 1.0F, y - 1.0F, 0.01F, f12, f13, f14, f11));
    //        }
    //
    //        if (!list.isEmpty()) {
    //            TexturedGlyph texturedglyph1 = this.font.getWhiteGlyph();
    //            IVertexBuilder ivertexbuilder1 = buffer.getBuffer(texturedglyph1.getRenderType(isTransparent));
    //
    //            for(TexturedGlyph.Effect texturedglyph$effect : list) {
    //                texturedglyph1.renderEffect(texturedglyph$effect, matrix, ivertexbuilder1, packedLight);
    //            }
    //        }

    public static RenderType createRenderType(Emoji emoji) {
        RenderType.State state = RenderType.State.getBuilder().texture(new RenderState.TextureState(emoji.getResourceLocationForBinding(), false, false)).transparency(new RenderState.TransparencyState("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }, () -> {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            RenderSystem.disableBlend();
        })).build(true);
        return RenderType.makeType("portal_render", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, state);
    }

    private float renderEmoji(Emoji emoji, float x, float y, Matrix4f matrix, IRenderTypeBuffer buffer, int packedLight) {
        float textureSize = 16;
        float textureX = 0 / textureSize;
        float textureY = 0 / textureSize;
        float textureOffset = 16.0F / textureSize;
        float size = 10f;
        float offsetY = 1.0F;
        float offsetX = 0.0F;

        IVertexBuilder builder = buffer.getBuffer(createRenderType(emoji));

        builder.pos(matrix, x - offsetX, y - offsetY, 0.0f).color(255, 255, 255, 255).tex(textureX, textureY).lightmap(packedLight).endVertex();
        builder.pos(matrix, x - offsetX, y + size - offsetY, 0.0F).color(255, 255, 255, 255).tex(textureX, textureY + textureOffset).lightmap(packedLight).endVertex();
        builder.pos(matrix, x - offsetX + size, y + size - offsetY, 0.0F).color(255, 255, 255, 255).tex(textureX + textureOffset, textureY + textureOffset).lightmap(packedLight).endVertex();
        builder.pos(matrix, x - offsetX + size, y - offsetY, 0.0F).color(255, 255, 255, 255).tex(textureX + textureOffset, textureY / textureSize).lightmap(packedLight).endVertex();
        return 10f;
    }

    @OnlyIn(Dist.CLIENT)
    class EmojiCharacterRenderer implements TextProcessing.ICharacterConsumer {
        final IRenderTypeBuffer buffer;
        private final boolean field_238429_c_;
        private final float field_238430_d_;
        private final float field_238431_e_;
        private final float field_238432_f_;
        private final float field_238433_g_;
        private final float field_238434_h_;
        private final Matrix4f matrix;
        private final boolean field_238436_j_;
        private final int packedLight;
        private float field_238438_l_;
        private float field_238439_m_;
        private HashMap<Integer, Emoji> emojis;
        @Nullable
        private List<TexturedGlyph.Effect> field_238440_n_;

        private void func_238442_a_(TexturedGlyph.Effect p_238442_1_) {
            if (this.field_238440_n_ == null) {
                this.field_238440_n_ = Lists.newArrayList();
            }

            this.field_238440_n_.add(p_238442_1_);
        }

        public EmojiCharacterRenderer(HashMap<Integer, Emoji> emojis, IRenderTypeBuffer p_i232250_2_, float p_i232250_3_, float p_i232250_4_, int p_i232250_5_, boolean p_i232250_6_, Matrix4f p_i232250_7_, boolean p_i232250_8_, int p_i232250_9_) {
            this.buffer = p_i232250_2_;
            this.emojis = emojis;
            this.field_238438_l_ = p_i232250_3_;
            this.field_238439_m_ = p_i232250_4_;
            this.field_238429_c_ = p_i232250_6_;
            this.field_238430_d_ = p_i232250_6_ ? 0.25F : 1.0F;
            this.field_238431_e_ = (float) (p_i232250_5_ >> 16 & 255) / 255.0F * this.field_238430_d_;
            this.field_238432_f_ = (float) (p_i232250_5_ >> 8 & 255) / 255.0F * this.field_238430_d_;
            this.field_238433_g_ = (float) (p_i232250_5_ & 255) / 255.0F * this.field_238430_d_;
            this.field_238434_h_ = (float) (p_i232250_5_ >> 24 & 255) / 255.0F;
            this.matrix = p_i232250_7_;
            this.field_238436_j_ = p_i232250_8_;
            this.packedLight = p_i232250_9_;
        }

        public boolean onChar(int p_onChar_1_, Style p_onChar_2_, int pos) {
            Font font = EmojiFontRenderer.this.func_238419_a_(p_onChar_2_.getFontId());
            if (EmojifulConfig.getInstance().renderEmoji.get() && this.emojis.get(p_onChar_1_) != null) {
                Emoji emoji = this.emojis.get(p_onChar_1_);
                if (emoji != null) {
                    EmojiFontRenderer.this.renderEmoji(emoji, this.field_238438_l_, this.field_238439_m_, matrix, buffer, packedLight);
                    this.field_238438_l_ += 10;
                    return true;
                }
            } else {
                IGlyph iglyph = font.func_238557_a_(pos);
                TexturedGlyph texturedglyph = p_onChar_2_.getObfuscated() && pos != 32 ? font.obfuscate(iglyph) : font.func_238559_b_(pos);
                boolean flag = p_onChar_2_.getBold();
                float f3 = this.field_238434_h_;
                Color color = p_onChar_2_.getColor();
                float f;
                float f1;
                float f2;
                if (color != null) {
                    int i = color.func_240742_a_();
                    f = (float) (i >> 16 & 255) / 255.0F * this.field_238430_d_;
                    f1 = (float) (i >> 8 & 255) / 255.0F * this.field_238430_d_;
                    f2 = (float) (i & 255) / 255.0F * this.field_238430_d_;
                } else {
                    f = this.field_238431_e_;
                    f1 = this.field_238432_f_;
                    f2 = this.field_238433_g_;
                }

                if (!(texturedglyph instanceof EmptyGlyph)) {
                    float f5 = flag ? iglyph.getBoldOffset() : 0.0F;
                    float f4 = this.field_238429_c_ ? iglyph.getShadowOffset() : 0.0F;
                    IVertexBuilder ivertexbuilder = this.buffer.getBuffer(texturedglyph.getRenderType(this.field_238436_j_));
                    EmojiFontRenderer.this.drawGlyph(texturedglyph, flag, p_onChar_2_.getItalic(), f5, this.field_238438_l_ + f4, this.field_238439_m_ + f4, this.matrix, ivertexbuilder, f, f1, f2, f3, this.packedLight);
                }

                float f6 = iglyph.getAdvance(flag);
                float f7 = this.field_238429_c_ ? 1.0F : 0.0F;
                if (p_onChar_2_.getStrikethrough()) {
                    this.func_238442_a_(new TexturedGlyph.Effect(this.field_238438_l_ + f7 - 1.0F, this.field_238439_m_ + f7 + 4.5F, this.field_238438_l_ + f7 + f6, this.field_238439_m_ + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
                }

                if (p_onChar_2_.getUnderlined()) {
                    this.func_238442_a_(new TexturedGlyph.Effect(this.field_238438_l_ + f7 - 1.0F, this.field_238439_m_ + f7 + 9.0F, this.field_238438_l_ + f7 + f6, this.field_238439_m_ + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
                }

                this.field_238438_l_ += f6;
                return true;
            }
            return false;
        }

        public float func_238441_a_(int p_238441_1_, float p_238441_2_) {
            if (p_238441_1_ != 0) {
                float f = (float) (p_238441_1_ >> 24 & 255) / 255.0F;
                float f1 = (float) (p_238441_1_ >> 16 & 255) / 255.0F;
                float f2 = (float) (p_238441_1_ >> 8 & 255) / 255.0F;
                float f3 = (float) (p_238441_1_ & 255) / 255.0F;
                this.func_238442_a_(new TexturedGlyph.Effect(p_238441_2_ - 1.0F, this.field_238439_m_ + 9.0F, this.field_238438_l_ + 1.0F, this.field_238439_m_ - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.field_238440_n_ != null) {
                TexturedGlyph texturedglyph = EmojiFontRenderer.this.func_238419_a_(Style.DEFAULT_FONT).getWhiteGlyph();
                IVertexBuilder ivertexbuilder = this.buffer.getBuffer(texturedglyph.getRenderType(this.field_238436_j_));

                for (TexturedGlyph.Effect texturedglyph$effect : this.field_238440_n_) {
                    texturedglyph.renderEffect(texturedglyph$effect, this.matrix, ivertexbuilder, this.packedLight);
                }
            }

            return this.field_238438_l_;
        }
    }

}
