package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.render.EmojiFontHelper;
import net.minecraft.client.StringSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StringSplitter.class)
public class StringSplitterMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static StringSplitter.WidthProvider emojiful$wrapWidthProvider(
            StringSplitter.WidthProvider original
    ) {
        return (codePoint, style) -> EmojiFontHelper.isWrappedEmoji(codePoint, style)
                ? EmojiFontHelper.EMOJI_ADVANCE
                : original.getWidth(codePoint, style);
    }
}
