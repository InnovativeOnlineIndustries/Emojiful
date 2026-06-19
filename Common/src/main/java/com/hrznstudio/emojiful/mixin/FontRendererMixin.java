package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.render.EmojiFontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Font.class)
public class FontRendererMixin {
    @ModifyVariable(
            method = {
                    "split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;",
                    "splitIgnoringLanguage(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"
            },
            at = @At("HEAD"),
            argsOnly = true
    )
    private FormattedText emojiful$prepareTextForWrapping(FormattedText text) {
        return EmojiFontHelper.prepareForWrapping(text);
    }

    @Inject(
            method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emojiful$prepareString(
            String text,
            float x,
            float y,
            int color,
            boolean shadow,
            int backgroundColor,
            CallbackInfoReturnable<Font.PreparedText> callback
    ) {
        if (EmojiFontHelper.BYPASS.get()) {
            return;
        }
        EmojiFontHelper.ParsedText parsed = EmojiFontHelper.parse(text);
        if (Objects.equals(parsed.text(), text) && parsed.emojis().isEmpty()) {
            return;
        }

        EmojiFontHelper.push(parsed.emojis());
        EmojiFontHelper.BYPASS.set(true);
        try {
            callback.setReturnValue(((Font) (Object) this).prepareText(
                    parsed.text(), x, y, color, shadow, backgroundColor
            ));
        } finally {
            EmojiFontHelper.BYPASS.set(false);
            EmojiFontHelper.pop();
        }
    }

    @Inject(
            method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emojiful$prepareSequence(
            FormattedCharSequence sequence,
            float x,
            float y,
            int color,
            boolean shadow,
            boolean includeEmpty,
            int backgroundColor,
            CallbackInfoReturnable<Font.PreparedText> callback
    ) {
        if (EmojiFontHelper.BYPASS.get()) {
            return;
        }
        EmojiFontHelper.ParsedSequence parsed = EmojiFontHelper.parseSequence(sequence);
        if (parsed.sequence() == sequence && parsed.parsed().emojis().isEmpty()) {
            return;
        }

        EmojiFontHelper.push(parsed.parsed().emojis());
        EmojiFontHelper.BYPASS.set(true);
        try {
            callback.setReturnValue(((Font) (Object) this).prepareText(
                    parsed.sequence(), x, y, color, shadow, includeEmpty, backgroundColor
            ));
        } finally {
            EmojiFontHelper.BYPASS.set(false);
            EmojiFontHelper.pop();
        }
    }

    @Inject(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void emojiful$widthString(String text, CallbackInfoReturnable<Integer> callback) {
        if (EmojiFontHelper.BYPASS.get()) {
            return;
        }
        EmojiFontHelper.ParsedText parsed = EmojiFontHelper.parse(text);
        if (!Objects.equals(parsed.text(), text) || !parsed.emojis().isEmpty()) {
            callback.setReturnValue(EmojiFontHelper.width((Font) (Object) this, text));
        }
    }

    @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
    private void emojiful$widthSequence(FormattedCharSequence sequence, CallbackInfoReturnable<Integer> callback) {
        if (!EmojiFontHelper.BYPASS.get()) {
            callback.setReturnValue(EmojiFontHelper.width((Font) (Object) this, sequence));
        }
    }

    @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
    private void emojiful$widthText(FormattedText text, CallbackInfoReturnable<Integer> callback) {
        if (!EmojiFontHelper.BYPASS.get()) {
            callback.setReturnValue(EmojiFontHelper.width((Font) (Object) this, text.getString()));
        }
    }
}
