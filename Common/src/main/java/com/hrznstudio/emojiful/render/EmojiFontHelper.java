package com.hrznstudio.emojiful.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.platform.Services;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public final class EmojiFontHelper {
    public static final String SCAPED_STRING = "\\\\\\\\";
    public static final int PLACEHOLDER = 0x2603;
    public static final float EMOJI_ADVANCE = 10.0F;
    public static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> false);
    private static final Constructor<Style> STYLE_CONSTRUCTOR = findStyleConstructor();
    private static final ThreadLocal<Deque<Map<Integer, Emoji>>> ACTIVE_EMOJIS =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<Style, Emoji> WRAPPED_EMOJIS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    public static final LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> RECENT_STRINGS =
            CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build(
                    new CacheLoader<>() {
                        @Override
                        public Pair<String, HashMap<Integer, Emoji>> load(String key) {
                            ParsedText parsed = parseUncached(key);
                            return Pair.of(parsed.text(), new LinkedHashMap<>(parsed.emojis()));
                        }
                    }
            );

    private EmojiFontHelper() {
    }

    private static Constructor<Style> findStyleConstructor() {
        try {
            Constructor<Style> constructor = Style.class.getDeclaredConstructor(
                    TextColor.class,
                    Integer.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    Boolean.class,
                    ClickEvent.class,
                    HoverEvent.class,
                    String.class,
                    FontDescription.class
            );
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.error("Could not access Minecraft text style constructor", exception);
            return null;
        }
    }

    public static ParsedText parse(String text) {
        if (text == null) {
            return new ParsedText(null, new LinkedHashMap<>());
        }
        try {
            Pair<String, HashMap<Integer, Emoji>> parsed = RECENT_STRINGS.get(text);
            return new ParsedText(parsed.getLeft(), new LinkedHashMap<>(parsed.getRight()));
        } catch (ExecutionException exception) {
            Constants.LOG.error("Could not parse emoji text", exception);
            return new ParsedText(text, new LinkedHashMap<>());
        }
    }

    private static ParsedText parseUncached(String text) {
        LinkedHashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        if (StringUtil.isNullOrEmpty(text)) {
            return new ParsedText(text, emojis);
        }
        if (text.startsWith(SCAPED_STRING)) {
            return new ParsedText(text.substring(SCAPED_STRING.length()), emojis);
        }
        if (!Services.CONFIG.renderEmoji() || Constants.EMOJI_LIST.isEmpty()) {
            return new ParsedText(text, emojis);
        }

        StringBuilder result = new StringBuilder(text.length());
        int cursor = 0;
        while (cursor < text.length()) {
            Match best = findNext(text, cursor);
            if (best == null) {
                result.append(text, cursor, text.length());
                break;
            }

            result.append(text, cursor, best.start());
            int placeholderPosition = result.length();
            result.appendCodePoint(PLACEHOLDER);
            emojis.put(placeholderPosition, best.emoji());
            cursor = best.end();
        }
        return new ParsedText(result.toString(), emojis);
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
        StringBuilder plainText = new StringBuilder();
        sequence.accept((position, style, codePoint) -> {
            int start = plainText.length();
            plainText.appendCodePoint(codePoint);
            input.add(new StyledCodePoint(start, plainText.length(), style, codePoint));
            return true;
        });

        String source = plainText.toString();
        ParsedText parsed = parse(source);
        if (parsed.text().equals(source)) {
            return new ParsedSequence(sequence, parsed);
        }

        List<StyledCodePoint> output = new ArrayList<>();
        int sourceOffset = source.startsWith(SCAPED_STRING) ? SCAPED_STRING.length() : 0;
        int outputOffset = 0;
        while (sourceOffset < source.length()) {
            Match match = source.startsWith(SCAPED_STRING) ? null : findNext(source, sourceOffset);
            int literalEnd = match == null ? source.length() : match.start();
            while (sourceOffset < literalEnd) {
                int codePoint = source.codePointAt(sourceOffset);
                int count = Character.charCount(codePoint);
                output.add(new StyledCodePoint(outputOffset, outputOffset + count, styleAt(input, sourceOffset), codePoint));
                sourceOffset += count;
                outputOffset += count;
            }
            if (match == null) {
                break;
            }
            output.add(new StyledCodePoint(outputOffset, outputOffset + 1, styleAt(input, match.start()), PLACEHOLDER));
            sourceOffset = match.end();
            outputOffset++;
        }

        FormattedCharSequence transformed = sink -> {
            for (StyledCodePoint character : output) {
                if (!sink.accept(character.start(), character.style(), character.codePoint())) {
                    return false;
                }
            }
            return true;
        };
        return new ParsedSequence(transformed, parsed);
    }

    public static FormattedText prepareForWrapping(FormattedText text) {
        String source = text.getString();
        ParsedText parsed = parse(source);
        if (parsed.emojis().isEmpty()) {
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
        Style marker = cloneStyle(style);
        WRAPPED_EMOJIS.put(marker, emoji);
        return marker;
    }

    private static Style cloneStyle(Style style) {
        if (STYLE_CONSTRUCTOR == null) {
            return style;
        }
        try {
            return STYLE_CONSTRUCTOR.newInstance(
                    style.getColor(),
                    style.getShadowColor(),
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

    public static void push(Map<Integer, Emoji> emojis) {
        ACTIVE_EMOJIS.get().push(emojis);
    }

    public static void pop() {
        Deque<Map<Integer, Emoji>> stack = ACTIVE_EMOJIS.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            ACTIVE_EMOJIS.remove();
        }
    }

    public static Emoji emojiAt(int position) {
        Deque<Map<Integer, Emoji>> stack = ACTIVE_EMOJIS.get();
        return stack.isEmpty() ? null : stack.peek().get(position);
    }

    public static boolean hasEmoji(String text) {
        try {
            Pair<String, HashMap<Integer, Emoji>> result = RECENT_STRINGS.get(text);
            return !result.getRight().isEmpty() || !result.getLeft().equals(text);
        } catch (ExecutionException exception) {
            Constants.LOG.error("Could not parse emoji text", exception);
            return false;
        }
    }

    public static void clearCache() {
        RECENT_STRINGS.invalidateAll();
    }

    public static int width(Font font, String original) {
        ParsedText parsed = parse(original);
        if (parsed.emojis().isEmpty()) {
            if (parsed.text().equals(original)) {
                return font.width(original);
            }
            return bypass(() -> font.width(parsed.text()));
        }

        float width = 0.0F;
        int start = 0;
        for (Integer position : parsed.emojis().keySet()) {
            if (position > start) {
                String literal = parsed.text().substring(start, position);
                width += bypass(() -> font.width(literal));
            }
            width += EMOJI_ADVANCE;
            start = position + Character.charCount(PLACEHOLDER);
        }
        if (start < parsed.text().length()) {
            String literal = parsed.text().substring(start);
            width += bypass(() -> font.width(literal));
        }
        return (int) Math.ceil(width);
    }

    public static int width(Font font, FormattedCharSequence original) {
        ParsedSequence parsed = parseSequence(original);
        if (parsed.parsed().emojis().isEmpty()) {
            return bypass(() -> font.width(parsed.sequence()));
        }

        final float[] width = {0.0F};
        parsed.sequence().accept((position, style, codePoint) -> {
            if (parsed.parsed().emojis().containsKey(position)) {
                width[0] += EMOJI_ADVANCE;
            } else {
                FormattedCharSequence character =
                        FormattedCharSequence.forward(new String(Character.toChars(codePoint)), style);
                width[0] += bypass(() -> font.width(character));
            }
            return true;
        });
        return (int) Math.ceil(width[0]);
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

    public record ParsedText(String text, LinkedHashMap<Integer, Emoji> emojis) {
    }

    public record ParsedSequence(FormattedCharSequence sequence, ParsedText parsed) {
    }

    private record Match(int start, int end, Emoji emoji) {
    }

    private record StyledCodePoint(int start, int end, Style style, int codePoint) {
    }

}
