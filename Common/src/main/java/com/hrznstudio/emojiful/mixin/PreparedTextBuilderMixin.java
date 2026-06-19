package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.render.EmojiFontHelper;
import com.hrznstudio.emojiful.render.EmojiGlyph;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class PreparedTextBuilderMixin {
    @Shadow
    public abstract boolean accept(int position, Style style, BakedGlyph glyph);

    @Inject(
            method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emojiful$acceptEmoji(
            int position,
            Style style,
            int codePoint,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (codePoint != EmojiFontHelper.PLACEHOLDER) {
            return;
        }
        Emoji emoji = EmojiFontHelper.emojiAt(position);
        if (emoji == null) {
            emoji = EmojiFontHelper.wrappedEmoji(style);
        }
        if (emoji != null) {
            callback.setReturnValue(accept(position, style, new EmojiGlyph(emoji)));
        }
    }
}
