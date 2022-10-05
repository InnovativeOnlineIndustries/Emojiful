package com.hrznstudio.emojiful.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.mixin.access.FontAccessor;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.util.EmojiUtil;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.netty.util.internal.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiFontRenderer extends Font {

    //<+(\w)+:+(\w)+>
    public static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    private static String MY_NAME = "DevNotWorkingRn";

    public static LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> RECENT_STRINGS = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Pair<String, HashMap<Integer, Emoji>>>() {
        @Override
        public Pair<String, HashMap<Integer, Emoji>> load(String key) throws Exception {
            return getEmojiFormattedString(key);
        }
    });

    public EmojiFontRenderer(Font fontRenderer) {
        super(((FontAccessor)(fontRenderer)).emojifulCommon_getFonts(), ((FontAccessor)(fontRenderer)).emojifulCommon_getFishyGlyphs());
    }

    private TextureAtlasSprite sprite;

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public int width(String text) {
        if (text != null) {
            try {
                text = RECENT_STRINGS.get(text.replaceAll(MY_NAME, MY_NAME + " :blobcatbolb: ")).getKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.width(text);
    }

    @Override
    public int width(FormattedText textProperties) {
        if (textProperties instanceof Component) {
            /* TODO
            try {
                //return super.width(new TextComponent(RECENT_STRINGS.get(textProperties.getString().replaceAll(MY_NAME, MY_NAME + " :blobcatbolb: ")).getKey()).setStyle(((TextComponent) textProperties).getStyle()));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/
        }
        return this.width(textProperties.getString());
    }

    @Override
    public int width(FormattedCharSequence processor) {
        StringBuilder builder = new StringBuilder();
        processor.accept((p_accept_1_, p_accept_2_, ch) -> {
            builder.append((char) ch);
            return true;
        });
        return width(builder.toString());
    }

    public static Pair<String, HashMap<Integer, Emoji>> getEmojiFormattedString(String text) {
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        if (Services.CONFIG.renderEmoji() && !StringUtil.isNullOrEmpty(text)) {
            String unformattedText = ChatFormatting.stripFormatting(text);
            if (StringUtil.isNullOrEmpty(unformattedText))
                return Pair.of(text, emojis);
            for (Emoji emoji : Constants.EMOJI_LIST) {
                Pattern pattern = emoji.getRegex();
                Matcher matcher = pattern.matcher(unformattedText);
                while (matcher.find()) {
                    if (!matcher.group().isEmpty()) {
                        String emojiText = matcher.group();
                        int index = text.indexOf(emojiText);
                        emojis.put(index, emoji);
                        HashMap<Integer, Emoji> clean = new LinkedHashMap<>();
                        for (Integer integer : new ArrayList<>(emojis.keySet())) {
                            if (integer > index) {
                                Emoji e = emojis.get(integer);
                                emojis.remove(integer);
                                clean.put(integer - emojiText.length() + 1, e);
                            }
                        }
                        emojis.putAll(clean);
                        unformattedText = unformattedText.replaceFirst(Pattern.quote(emojiText), "\u2603");
                        text = text.replaceFirst("(?i)" + Pattern.quote(emojiText), "\u2603");
                    }
                }
            }
        }
        return Pair.of(text, emojis);
    }

    @Override
    public int drawInBatch(String p_228079_1_, float p_228079_2_, float p_228079_3_, int p_228079_4_, boolean p_228079_5_, Matrix4f p_228079_6_, MultiBufferSource p_228079_7_, boolean p_228079_8_, int p_228079_9_, int p_228079_10_) {
        return super.drawInBatch(p_228079_1_, p_228079_2_, p_228079_3_, p_228079_4_, p_228079_5_, p_228079_6_, p_228079_7_, p_228079_8_, p_228079_9_, p_228079_10_);
    }

    @Override
    public float renderText(String text, float x, float y, int color, boolean isShadow, Matrix4f matrix, MultiBufferSource buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
        if (text.isEmpty())
            return 0;
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        try {
            Pair<String, HashMap<Integer, Emoji>> cache = RECENT_STRINGS.get(text.replaceAll(MY_NAME, MY_NAME + " :blobcatbolb: "));
            text = cache.getLeft();
            emojis = cache.getRight();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, isShadow, matrix, isTransparent, packedLight);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, fontrenderer$characterrenderer);
        return fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
    }

    @Override
    public int drawInBatch(FormattedCharSequence reorderingProcessor, float x, float y, int color, boolean isShadow, Matrix4f matrix, MultiBufferSource buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
        if (reorderingProcessor != null) {
            StringBuilder builder = new StringBuilder();
            if (reorderingProcessor != null) {
                reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                    builder.append((char) ch);
                    return true;
                });
            }
            String text = builder.toString().replaceAll(MY_NAME, MY_NAME + " :blobcatbolb:");
            if (text.length() > 0) {
                color = (color & -67108864) == 0 ? color | -16777216 : color;
                HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
                try {
                    Pair<String, HashMap<Integer, Emoji>> cache = RECENT_STRINGS.get(text);
                    text = cache.getLeft();
                    emojis = cache.getRight();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                List<FormattedCharSequence> processors = new ArrayList<>();
                HashMap<Integer, Emoji> finalEmojis = emojis;
                AtomicInteger cleanPos = new AtomicInteger();
                AtomicBoolean ignore = new AtomicBoolean(false);
                reorderingProcessor.accept((pos, style, ch) -> {
                    if (!ignore.get()) {
                        if (finalEmojis.get(cleanPos.get()) == null) {
                            processors.add(new CharacterProcessor(cleanPos.getAndIncrement(), style, ch));
                        } else {
                            processors.add(new CharacterProcessor(cleanPos.get(), style, ' '));
                            ignore.set(true);
                            return true;
                        }
                    }
                    if (ch == ':') {
                        ignore.set(false);
                        cleanPos.getAndIncrement();
                    }
                    return true;
                });
                StringBuilder builder2 = new StringBuilder();
                FormattedCharSequence.fromList(processors).accept((p_accept_1_, p_accept_2_, ch) -> {
                    builder2.append((char) ch);
                    return true;
                });
                Matrix4f matrix4f = matrix.copy();
                if (isShadow) {
                    EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, true, matrix, isTransparent, packedLight);
                    FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                    fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
                    matrix4f.translate(SHADOW_OFFSET);
                }
                EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, false, matrix4f, isTransparent, packedLight);
                FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                return (int) fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
            }
        }
        return super.drawInBatch(reorderingProcessor, x, y, color, isShadow, matrix, buffer, isTransparent, colorBackgroundIn, packedLight);
    }

    class CharacterProcessor implements FormattedCharSequence {

        public final int pos;
        public final Style style;
        public final int character;

        CharacterProcessor(int pos, Style style, int character) {
            this.pos = pos;
            this.style = style;
            this.character = character;
        }

        @Override
        public boolean accept(FormattedCharSink iCharacterConsumer) {
            return iCharacterConsumer.accept(pos, style, character);
        }
    }

    class EmojiCharacterRenderer implements FormattedCharSink {
        final MultiBufferSource buffer;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f matrix;
        private final boolean seeThrough;
        private final int packedLight;
        private float x;
        private float y;
        private HashMap<Integer, Emoji> emojis;
        @Nullable
        private List<BakedGlyph.Effect> effects;

        private void addEffect(BakedGlyph.Effect p_238442_1_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_238442_1_);
        }

        public EmojiCharacterRenderer(HashMap<Integer, Emoji> emojis, MultiBufferSource p_i232250_2_, float p_i232250_3_, float p_i232250_4_, int p_i232250_5_, boolean p_i232250_6_, Matrix4f p_i232250_7_, boolean p_i232250_8_, int p_i232250_9_) {
            this.buffer = p_i232250_2_;
            this.emojis = emojis;
            this.x = p_i232250_3_;
            this.y = p_i232250_4_;
            this.dropShadow = p_i232250_6_;
            this.dimFactor = p_i232250_6_ ? 0.25F : 1.0F;
            this.r = (float) (p_i232250_5_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float) (p_i232250_5_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float) (p_i232250_5_ & 255) / 255.0F * this.dimFactor;
            this.a = (float) (p_i232250_5_ >> 24 & 255) / 255.0F;
            this.matrix = p_i232250_7_;
            this.seeThrough = p_i232250_8_;
            this.packedLight = p_i232250_9_;
        }

        public boolean accept(int pos, Style style, int charInt) {
            FontSet font = ((FontAccessor)(EmojiFontRenderer.this)).emojifulCommon_getFontSet(style.getFont());
            if (Services.CONFIG.renderEmoji() && this.emojis.get(pos) != null) {
                Emoji emoji = this.emojis.get(pos);
                if (emoji != null && !this.dropShadow) {
                    EmojiUtil.renderEmoji(emoji, this.x, this.y, matrix, buffer, packedLight);
                    this.x += 10;
                    return true;
                }
            } else {
                GlyphInfo iglyph = font.getGlyphInfo(charInt, ((FontAccessor)(EmojiFontRenderer.this)).emojifulCommon_getFishyGlyphs());
                BakedGlyph texturedglyph = style.isObfuscated() && charInt != 32 ? font.getRandomGlyph(iglyph) : font.getGlyph(charInt);
                boolean flag = style.isBold();
                float f3 = this.a;
                TextColor color = style.getColor();
                float f;
                float f1;
                float f2;
                if (color != null) {
                    int i = color.getValue();
                    f = (float) (i >> 16 & 255) / 255.0F * this.dimFactor;
                    f1 = (float) (i >> 8 & 255) / 255.0F * this.dimFactor;
                    f2 = (float) (i & 255) / 255.0F * this.dimFactor;
                } else {
                    f = this.r;
                    f1 = this.g;
                    f2 = this.b;
                }

                if (!(texturedglyph instanceof EmptyGlyph)) {
                    float f5 = flag ? iglyph.getBoldOffset() : 0.0F;
                    float f4 = this.dropShadow ? iglyph.getShadowOffset() : 0.0F;
                    VertexConsumer ivertexbuilder = this.buffer.getBuffer(texturedglyph.renderType(this.seeThrough ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL));
                    ((FontAccessor)(EmojiFontRenderer.this)).emojifulCommon_renderChar(texturedglyph, flag, style.isItalic(), f5, this.x + f4, this.y + f4, this.matrix, ivertexbuilder, f, f1, f2, f3, this.packedLight);
                }

                float f6 = iglyph.getAdvance(flag);
                float f7 = this.dropShadow ? 1.0F : 0.0F;
                if (style.isStrikethrough()) {
                    this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
                }

                if (style.isUnderlined()) {
                    this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
                }

                this.x += f6;
                return true;
            }
            return false;
        }

        public float finish(int p_238441_1_, float p_238441_2_) {
            if (p_238441_1_ != 0) {
                float f = (float) (p_238441_1_ >> 24 & 255) / 255.0F;
                float f1 = (float) (p_238441_1_ >> 16 & 255) / 255.0F;
                float f2 = (float) (p_238441_1_ >> 8 & 255) / 255.0F;
                float f3 = (float) (p_238441_1_ & 255) / 255.0F;
                this.addEffect(new BakedGlyph.Effect(p_238441_2_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null) {
                try (FontSet fontSet = ((FontAccessor)(EmojiFontRenderer.this)).emojifulCommon_getFontSet(Style.DEFAULT_FONT)){
                    BakedGlyph texturedglyph = fontSet.whiteGlyph();
                    VertexConsumer ivertexbuilder = this.buffer.getBuffer(texturedglyph.renderType(this.seeThrough ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL));

                    for (BakedGlyph.Effect texturedglyph$effect : this.effects) {
                        texturedglyph.renderEffect(texturedglyph$effect, this.matrix, ivertexbuilder, this.packedLight);
                    }
                } catch (Exception e){
                    Constants.LOG.error("An error occured while rendering effects", e);
                }

            }

            return this.x;
        }
    }

}
