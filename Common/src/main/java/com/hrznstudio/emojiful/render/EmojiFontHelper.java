package com.hrznstudio.emojiful.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.util.EmojiUtil;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.netty.util.internal.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmojiFontHelper {

    public static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    public static final int PLACEHOLDER = 0x2603;
    public static final float EMOJI_ADVANCE = 10.0F;
    public static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> false);
    private static final Constructor<Style> STYLE_CONSTRUCTOR = findStyleConstructor();
    private static final Map<Style, Emoji> WRAPPED_EMOJIS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    public static LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> RECENT_STRINGS = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Pair<String, HashMap<Integer, Emoji>>>() {
        @Override
        public Pair<String, HashMap<Integer, Emoji>> load(String key) throws Exception {
            return getEmojiFormattedString(key);
        }
    });
    public static String SCAPED_STRING = "\\\\\\\\";
    //<+(\w)+:+(\w)+>

    public EmojiFontHelper() {

    }

    private static Constructor<Style> findStyleConstructor() {
        try {
            Constructor<Style> constructor = Style.class.getDeclaredConstructor(
                    TextColor.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    ClickEvent.class,
                    HoverEvent.class,
                    String.class,
                    ResourceLocation.class
            );
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.error("Could not access Minecraft text style constructor", exception);
            return null;
        }
    }

    public static Pair<String, HashMap<Integer, Emoji>> getEmojiFormattedString(String text) {
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        if (Services.CONFIG.renderEmoji() && !StringUtil.isNullOrEmpty(text)) {
            String unformattedText = ChatFormatting.stripFormatting(text);
            if (StringUtil.isNullOrEmpty(unformattedText))
                return Pair.of(text, emojis);
            if (text.startsWith(SCAPED_STRING)){
                return Pair.of(text, emojis);
            }
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
                        unformattedText = unformattedText.replaceFirst(Pattern.quote(emojiText), new String(Character.toChars(PLACEHOLDER)));
                        text = text.replaceFirst("(?i)" + Pattern.quote(emojiText), new String(Character.toChars(PLACEHOLDER)));
                    }
                }
            }
        }
        return Pair.of(text, emojis);
    }

    private static Match findNext(String text, int cursor) {
        Match best = null;
        for (Emoji emoji : Constants.EMOJI_LIST) {
            Matcher matcher = emoji.getRegex().matcher(text);
            matcher.region(cursor, text.length());
            if (!matcher.find() || matcher.start() == matcher.end()) {
                continue;
            }
            if (best == null
                    || matcher.start() < best.start()
                    || matcher.start() == best.start() && matcher.end() > best.end()) {
                best = new Match(matcher.start(), matcher.end(), emoji);
            }
        }
        return best;
    }

    public static ParsedSequence parseSequence(FormattedCharSequence sequence) {
        List<StyledCodePoint> input = new ArrayList<>();
        LinkedHashMap<Integer, Emoji> wrappedEmojis = new LinkedHashMap<>();
        StringBuilder plainText = new StringBuilder();
        sequence.accept((position, style, codePoint) -> {
            int start = plainText.length();
            plainText.appendCodePoint(codePoint);
            input.add(new StyledCodePoint(start, plainText.length(), style, codePoint));
            Emoji wrappedEmoji = isWrappedEmoji(codePoint, style) ? wrappedEmoji(style) : null;
            if (wrappedEmoji != null) {
                wrappedEmojis.put(start, wrappedEmoji);
            }
            return true;
        });

        String source = plainText.toString();
        Pair<String, HashMap<Integer, Emoji>> parsed;
        try {
            parsed = RECENT_STRINGS.get(source);
        } catch (ExecutionException exception) {
            Constants.LOG.error("Could not parse emoji text", exception);
            return new ParsedSequence(sequence, new LinkedHashMap<>());
        }
        if (source.startsWith(SCAPED_STRING)) {
            return new ParsedSequence(
                    FormattedCharSequence.forward(source.substring(SCAPED_STRING.length()), Style.EMPTY),
                    new LinkedHashMap<>()
            );
        }
        if (parsed.getLeft().equals(source) && parsed.getRight().isEmpty()) {
            if (!wrappedEmojis.isEmpty()) {
                return new ParsedSequence(sequence, wrappedEmojis);
            }
            return new ParsedSequence(sequence, new LinkedHashMap<>());
        }

        List<FormattedCharSequence> output = new ArrayList<>();
        int sourceOffset = source.startsWith(SCAPED_STRING) ? SCAPED_STRING.length() : 0;
        int outputOffset = 0;
        while (sourceOffset < source.length()) {
            Match match = source.startsWith(SCAPED_STRING) ? null : findNext(source, sourceOffset);
            int literalEnd = match == null ? source.length() : match.start();
            while (sourceOffset < literalEnd) {
                int codePoint = source.codePointAt(sourceOffset);
                int count = Character.charCount(codePoint);
                output.add(new CharacterProcessor(outputOffset, styleAt(input, sourceOffset), codePoint));
                sourceOffset += count;
                outputOffset += count;
            }
            if (match == null) {
                break;
            }
            output.add(new CharacterProcessor(outputOffset, styleAt(input, match.start()), PLACEHOLDER));
            sourceOffset = match.end();
            outputOffset++;
        }

        return new ParsedSequence(FormattedCharSequence.fromList(output), new LinkedHashMap<>(parsed.getRight()));
    }

    public static FormattedText prepareForWrapping(FormattedText text) {
        String source = text.getString();
        Pair<String, HashMap<Integer, Emoji>> parsed;
        try {
            parsed = RECENT_STRINGS.get(source);
        } catch (ExecutionException exception) {
            Constants.LOG.error("Could not parse emoji text", exception);
            return text;
        }
        if (parsed.getRight().isEmpty()) {
            return text;
        }

        List<FormattedText> parts = new ArrayList<>();
        text.visit((style, contents) -> {
            addWrappedParts(parts, contents, style);
            return java.util.Optional.empty();
        }, Style.EMPTY);
        return FormattedText.composite(parts);
    }

    private static void addWrappedParts(List<FormattedText> parts, String contents, Style style) {
        if (StringUtil.isNullOrEmpty(contents) || contents.startsWith(SCAPED_STRING)) {
            parts.add(FormattedText.of(contents, style));
            return;
        }

        int cursor = 0;
        while (cursor < contents.length()) {
            Match match = findNext(contents, cursor);
            if (match == null) {
                parts.add(FormattedText.of(contents.substring(cursor), style));
                return;
            }
            if (match.start() > cursor) {
                parts.add(FormattedText.of(contents.substring(cursor, match.start()), style));
            }
            parts.add(FormattedText.of(new String(Character.toChars(PLACEHOLDER)), registerWrappedEmoji(style, match.emoji())));
            cursor = match.end();
        }
    }

    private static Style registerWrappedEmoji(Style style, Emoji emoji) {
        Style marker = cloneStyle(style).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createTooltip(emoji)));
        WRAPPED_EMOJIS.put(marker, emoji);
        return marker;
    }

    private static Component createTooltip(Emoji emoji) {
        MutableComponent tooltip = Component.literal(SCAPED_STRING + "Emoji: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(emoji.name).withStyle(ChatFormatting.GOLD));
        String category = categoryFor(emoji);
        if (category != null) {
            tooltip.append(Component.literal("\n" + SCAPED_STRING + "Category: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(category).withStyle(ChatFormatting.AQUA));
        }
        if (!emoji.strings.isEmpty()) {
            String aliases = emoji.strings.stream()
                    .limit(8)
                    .map(alias -> SCAPED_STRING + alias)
                    .collect(Collectors.joining(", "));
            tooltip.append(Component.literal("\n" + SCAPED_STRING + "Aliases: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(aliases).withStyle(ChatFormatting.GREEN));
            if (emoji.strings.size() > 8) {
                tooltip.append(Component.literal(" +" + (emoji.strings.size() - 8) + " more").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return tooltip;
    }

    private static String categoryFor(Emoji emoji) {
        for (Map.Entry<String, List<Emoji>> entry : Constants.EMOJI_MAP.entrySet()) {
            if (entry.getValue().contains(emoji)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static Style cloneStyle(Style style) {
        if (STYLE_CONSTRUCTOR == null) {
            return style;
        }
        try {
            return STYLE_CONSTRUCTOR.newInstance(
                    style.getColor(),
                    style.isBold() ? Boolean.TRUE : null,
                    style.isItalic() ? Boolean.TRUE : null,
                    style.isUnderlined() ? Boolean.TRUE : null,
                    style.isStrikethrough() ? Boolean.TRUE : null,
                    style.isObfuscated() ? Boolean.TRUE : null,
                    style.getClickEvent(),
                    style.getHoverEvent(),
                    style.getInsertion(),
                    style.getFont()
            );
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.error("Could not clone Minecraft text style", exception);
            return style;
        }
    }

    public static Emoji wrappedEmoji(Style style) {
        return WRAPPED_EMOJIS.get(style);
    }

    public static boolean isWrappedEmoji(int codePoint, Style style) {
        return codePoint == PLACEHOLDER && wrappedEmoji(style) != null;
    }

    private static Style styleAt(List<StyledCodePoint> characters, int offset) {
        for (StyledCodePoint character : characters) {
            if (offset >= character.start() && offset < character.end()) {
                return character.style();
            }
        }
        return characters.isEmpty() ? Style.EMPTY : characters.get(characters.size() - 1).style();
    }

    public static int width(Font font, String original) {
        // Font#width(String) accepts null and treats it as an empty string. Keep
        // that vanilla contract before using the cache, which rejects null keys.
        if (original == null) {
            return 0;
        }

        Pair<String, HashMap<Integer, Emoji>> parsed;
        try {
            parsed = RECENT_STRINGS.get(original);
        } catch (ExecutionException exception) {
            Constants.LOG.error("Could not parse emoji text", exception);
            return font.width(original);
        }
        if (parsed.getRight().isEmpty()) {
            return bypass(() -> font.width(parsed.getLeft().replace(SCAPED_STRING, "")));
        }

        float width = 0.0F;
        int start = 0;
        for (Integer position : parsed.getRight().keySet()) {
            if (position > start) {
                String literal = parsed.getLeft().substring(start, position);
                width += bypass(() -> font.width(literal));
            }
            width += EMOJI_ADVANCE;
            start = position + Character.charCount(PLACEHOLDER);
        }
        if (start < parsed.getLeft().length()) {
            String literal = parsed.getLeft().substring(start);
            width += bypass(() -> font.width(literal));
        }
        return (int) Math.ceil(width);
    }

    public static int width(Font font, FormattedCharSequence original) {
        ParsedSequence parsed = parseSequence(original);
        if (parsed.emojis().isEmpty()) {
            return bypass(() -> font.width(parsed.sequence()));
        }

        final float[] width = {0.0F};
        parsed.sequence().accept((position, style, codePoint) -> {
            if (parsed.emojis().containsKey(position) || isWrappedEmoji(codePoint, style)) {
                width[0] += EMOJI_ADVANCE;
            } else {
                FormattedCharSequence character = FormattedCharSequence.forward(new String(Character.toChars(codePoint)), style);
                width[0] += bypass(() -> font.width(character));
            }
            return true;
        });
        return (int) Math.ceil(width[0]);
    }

    public static void clearCache() {
        RECENT_STRINGS.invalidateAll();
    }

    private static <T> T bypass(java.util.function.Supplier<T> action) {
        boolean previous = BYPASS.get();
        BYPASS.set(true);
        try {
            return action.get();
        } finally {
            BYPASS.set(previous);
        }
    }

    public static class CharacterProcessor implements FormattedCharSequence {

        public final int pos;
        public final Style style;
        public final int character;

        public CharacterProcessor(int pos, Style style, int character) {
            this.pos = pos;
            this.style = style;
            this.character = character;
        }

        @Override
        public boolean accept(FormattedCharSink iCharacterConsumer) {
            return iCharacterConsumer.accept(pos, style, character);
        }
    }

    public static class EmojiCharacterRenderer implements FormattedCharSink {
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
        private final float y;
        private final HashMap<Integer, Emoji> emojis;
        @Nullable
        private List<BakedGlyph.Effect> effects;

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

        private void addEffect(BakedGlyph.Effect p_238442_1_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_238442_1_);
        }

        public boolean accept(int pos, Style style, int charInt) {
            FontSet font = Minecraft.getInstance().font.getFontSet(style.getFont());
            Emoji wrappedEmoji = charInt == PLACEHOLDER ? wrappedEmoji(style) : null;
            if (Services.CONFIG.renderEmoji() && (this.emojis.get(pos) != null || wrappedEmoji != null)) {
                Emoji emoji = wrappedEmoji != null ? wrappedEmoji : this.emojis.get(pos);
                if (emoji != null) {
                    if (!this.dropShadow) EmojiUtil.renderEmoji(emoji, this.x, this.y, matrix, buffer, packedLight);
                    this.x += EMOJI_ADVANCE;
                }
            } else {
                GlyphInfo iglyph = font.getGlyphInfo(charInt, Minecraft.getInstance().font.filterFishyGlyphs);
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
                    VertexConsumer ivertexbuilder = this.buffer.getBuffer(texturedglyph.renderType(this.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL));
                    Minecraft.getInstance().font.renderChar(texturedglyph, flag, style.isItalic(), f5, this.x + f4, this.y + f4, this.matrix, ivertexbuilder, f, f1, f2, f3, this.packedLight);
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
            return true;
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
                FontSet fontSet = Minecraft.getInstance().font.getFontSet(Style.DEFAULT_FONT);
                BakedGlyph texturedglyph = fontSet.whiteGlyph();
                VertexConsumer ivertexbuilder = this.buffer.getBuffer(texturedglyph.renderType(this.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL));

                for (BakedGlyph.Effect texturedglyph$effect : this.effects) {
                    texturedglyph.renderEffect(texturedglyph$effect, this.matrix, ivertexbuilder, this.packedLight);
                }
            }

            return this.x;
        }
    }

    public record ParsedSequence(FormattedCharSequence sequence, LinkedHashMap<Integer, Emoji> emojis) {
    }

    private record Match(int start, int end, Emoji emoji) {
    }

    private record StyledCodePoint(int start, int end, Style style, int codePoint) {
    }

}
