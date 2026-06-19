package com.hrznstudio.emojiful.render;

import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.Emoji;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.stream.Collectors;

public final class EmojiGlyph implements BakedGlyph {
    private static final GlyphInfo INFO = GlyphInfo.simple(EmojiFontHelper.EMOJI_ADVANCE);
    private final Emoji emoji;

    public EmojiGlyph(Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public GlyphInfo info() {
        return INFO;
    }

    @Override
    public TextRenderable.Styled createGlyph(
            float x,
            float y,
            int color,
            int shadowColor,
            Style style,
            float boldOffset,
            float shadowOffset
    ) {
        Identifier identifier = emoji.getResourceLocationForBinding();
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
        BakedSheetGlyph glyph = new BakedSheetGlyph(
                INFO,
                GlyphRenderTypes.createForColorTexture(identifier),
                texture.getTextureView(),
                0.0F,
                1.0F,
                0.0F,
                1.0F,
                0.0F,
                EmojiFontHelper.EMOJI_ADVANCE,
                -1.0F,
                9.0F
        );
        Style emojiStyle = style.withHoverEvent(new HoverEvent.ShowText(createTooltip()));
        return glyph.createGlyph(x, y, 0xFFFFFFFF, 0, emojiStyle, 0.0F, 0.0F);
    }

    private Component createTooltip() {
        MutableComponent tooltip = Component.literal(EmojiFontHelper.SCAPED_STRING + "Emoji: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(displayName()).withStyle(ChatFormatting.YELLOW));

        String category = category();
        if (category != null) {
            tooltip.append(Component.literal("\nCategory: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(category).withStyle(ChatFormatting.WHITE));
        }

        String aliases = emoji.strings.stream()
                .distinct()
                .limit(8)
                .collect(Collectors.joining(", " + EmojiFontHelper.SCAPED_STRING));
        if (!aliases.isEmpty()) {
            tooltip.append(Component.literal("\n" + EmojiFontHelper.SCAPED_STRING + "Aliases: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(aliases).withStyle(ChatFormatting.WHITE));
        }
        return tooltip;
    }

    private String displayName() {
        String name = emoji.name == null || emoji.name.isBlank()
                ? emoji.getShorterString().replace(":", "")
                : emoji.name;
        return name.replaceFirst("^(twemojis_|custom_)", "")
                .replace('_', ' ')
                .replace('-', ' ');
    }

    private String category() {
        for (Map.Entry<String, java.util.List<Emoji>> entry : Constants.EMOJI_MAP.entrySet()) {
            if (entry.getValue().contains(emoji)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
