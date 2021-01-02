package com.hrznstudio.emojiful.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.hrznstudio.emojiful.Emojiful;
import com.hrznstudio.emojiful.EmojifulConfig;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.util.EmojiUtil;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.EmptyGlyph;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.IGlyph;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiFontRenderer extends FontRenderer {

    //<+(\w)+:+(\w)+>

    private static String MY_NAME = "DevNotWorkingRn";

    public static LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> RECENT_STRINGS = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Pair<String, HashMap<Integer, Emoji>>>() {
        @Override
        public Pair<String, HashMap<Integer, Emoji>> load(String key) throws Exception {
            return getEmojiFormattedString(key);
        }
    });

    public EmojiFontRenderer(FontRenderer fontRenderer) {
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
                text = RECENT_STRINGS.get(text.replaceAll(MY_NAME, MY_NAME + " :blobcatbolb: ")).getKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.getStringWidth(text);
    }

    @Override
    public int getStringPropertyWidth(ITextProperties textProperties) {
        if (textProperties instanceof StringTextComponent){
            try {
                return super.getStringPropertyWidth(new StringTextComponent(RECENT_STRINGS.get(textProperties.getString().replaceAll(MY_NAME, MY_NAME + " :blobcatbolb: ")).getKey()).setStyle(((StringTextComponent) textProperties).getStyle()));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return this.getStringWidth(textProperties.getString());
    }

    @Override
    public int func_243245_a(IReorderingProcessor processor) {
        StringBuilder builder = new StringBuilder();
        processor.accept((p_accept_1_, p_accept_2_, ch) -> {
            builder.append((char) ch);
            return true;
        });
        return getStringWidth(builder.toString());
    }

    public static Pair<String, HashMap<Integer, Emoji>> getEmojiFormattedString(String text) {
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        if (EmojifulConfig.getInstance().renderEmoji.get() && !StringUtil.isNullOrEmpty(text)) {
            String unformattedText = TextFormatting.getTextWithoutFormattingCodes(text);
            if (StringUtil.isNullOrEmpty(unformattedText))
                return Pair.of(text, emojis);
            for (Emoji emoji : Emojiful.EMOJI_LIST) {
                Pattern pattern = emoji.getRegex();
                Matcher matcher = pattern.matcher(unformattedText);
                while (matcher.find()){
                    if (!matcher.group().isEmpty()){
                        String emojiText = matcher.group();
                        int index = text.indexOf(emojiText);
                        emojis.put(index, emoji);
                        HashMap<Integer, Emoji> clean = new LinkedHashMap<>();
                        for (Integer integer : new ArrayList<>(emojis.keySet())) {
                            if (integer > index){
                                Emoji e = emojis.get(integer);
                                emojis.remove(integer);
                                clean.put(integer-emojiText.length() +1, e);
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
    public int renderString(String p_228079_1_, float p_228079_2_, float p_228079_3_, int p_228079_4_, boolean p_228079_5_, Matrix4f p_228079_6_, IRenderTypeBuffer p_228079_7_, boolean p_228079_8_, int p_228079_9_, int p_228079_10_) {
        return super.renderString(p_228079_1_, p_228079_2_, p_228079_3_, p_228079_4_, p_228079_5_, p_228079_6_, p_228079_7_, p_228079_8_, p_228079_9_, p_228079_10_);
    }

    @Override
    protected float renderStringAtPos(String text, float x, float y, int color, boolean isShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
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
        TextProcessing.func_238346_c_(text, Style.EMPTY, fontrenderer$characterrenderer);
        return fontrenderer$characterrenderer.func_238441_a_(colorBackgroundIn, x);
    }

    @Override
    public int func_238416_a_(IReorderingProcessor reorderingProcessor, float x, float y, int color, boolean isShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean isTransparent, int colorBackgroundIn, int packedLight) {
        StringBuilder builder = new StringBuilder();
        reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
            builder.append((char) ch);
            return true;
        });
        String text = builder.toString().replaceAll(MY_NAME, MY_NAME + " :blobcatbolb:");
        if (text.length() > 0){
            color = (color & -67108864) == 0 ? color | -16777216 : color;
            HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
            try {
                Pair<String, HashMap<Integer, Emoji>> cache = RECENT_STRINGS.get(text);
                text = cache.getLeft();
                emojis = cache.getRight();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            List<IReorderingProcessor> processors = new ArrayList<>();
            HashMap<Integer, Emoji> finalEmojis = emojis;
            AtomicInteger cleanPos = new AtomicInteger();
            AtomicBoolean ignore = new AtomicBoolean(false);
            reorderingProcessor.accept((pos, style, ch) -> {
                if (!ignore.get()){
                    if (finalEmojis.get(cleanPos.get()) == null){
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
            IReorderingProcessor.func_242247_b(processors).accept((p_accept_1_, p_accept_2_, ch) -> {
                builder2.append((char) ch);
                return true;
            });
            Matrix4f matrix4f = matrix.copy();
            if (isShadow) {
                EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, true, matrix, isTransparent, packedLight);
                IReorderingProcessor.func_242247_b(processors).accept(fontrenderer$characterrenderer);
                fontrenderer$characterrenderer.func_238441_a_(colorBackgroundIn, x);
                matrix4f.translate(field_238401_c_);
            }
            EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiCharacterRenderer(emojis, buffer, x, y, color, false, matrix4f, isTransparent, packedLight);
            IReorderingProcessor.func_242247_b(processors).accept(fontrenderer$characterrenderer);
            return (int) fontrenderer$characterrenderer.func_238441_a_(colorBackgroundIn, x);
        }
        return super.func_238416_a_(reorderingProcessor,x,y,color, isShadow, matrix, buffer, isTransparent, colorBackgroundIn, packedLight);
    }

    class CharacterProcessor implements IReorderingProcessor{

        public final int pos;
        public final Style style;
        public final int character;

        CharacterProcessor(int pos, Style style, int character) {
            this.pos = pos;
            this.style = style;
            this.character = character;
        }

        @Override
        public boolean accept(ICharacterConsumer iCharacterConsumer) {
            return iCharacterConsumer.accept(pos, style, character);
        }
    }


    @OnlyIn(Dist.CLIENT)
    class EmojiCharacterRenderer implements ICharacterConsumer {
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

        public boolean accept(int pos, Style style, int charInt) {
            Font font = EmojiFontRenderer.this.getFont(style.getFontId());
            if (EmojifulConfig.getInstance().renderEmoji.get() && this.emojis.get(pos) != null) {
                Emoji emoji = this.emojis.get(pos);
                if (emoji != null && !this.field_238429_c_) {
                    EmojiUtil.renderEmoji(emoji, this.field_238438_l_, this.field_238439_m_, matrix, buffer, packedLight);
                    this.field_238438_l_ += 10;
                    return true;
                }
            } else {
                IGlyph iglyph = font.func_238557_a_(charInt);
                TexturedGlyph texturedglyph = style.getObfuscated() && charInt != 32 ? font.obfuscate(iglyph) : font.func_238559_b_(charInt);
                boolean flag = style.getBold();
                float f3 = this.field_238434_h_;
                Color color = style.getColor();
                float f;
                float f1;
                float f2;
                if (color != null) {
                    int i = color.getColor();
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
                    EmojiFontRenderer.this.drawGlyph(texturedglyph, flag, style.getItalic(), f5, this.field_238438_l_ + f4, this.field_238439_m_ + f4, this.matrix, ivertexbuilder, f, f1, f2, f3, this.packedLight);
                }

                float f6 = iglyph.getAdvance(flag);
                float f7 = this.field_238429_c_ ? 1.0F : 0.0F;
                if (style.getStrikethrough()) {
                    this.func_238442_a_(new TexturedGlyph.Effect(this.field_238438_l_ + f7 - 1.0F, this.field_238439_m_ + f7 + 4.5F, this.field_238438_l_ + f7 + f6, this.field_238439_m_ + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
                }

                if (style.getUnderlined()) {
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
                TexturedGlyph texturedglyph = EmojiFontRenderer.this.getFont(Style.DEFAULT_FONT).getWhiteGlyph();
                IVertexBuilder ivertexbuilder = this.buffer.getBuffer(texturedglyph.getRenderType(this.field_238436_j_));

                for (TexturedGlyph.Effect texturedglyph$effect : this.field_238440_n_) {
                    texturedglyph.renderEffect(texturedglyph$effect, this.matrix, ivertexbuilder, this.packedLight);
                }
            }

            return this.field_238438_l_;
        }
    }

}
