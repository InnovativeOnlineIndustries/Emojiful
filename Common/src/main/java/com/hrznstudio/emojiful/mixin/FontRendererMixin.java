package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.render.EmojiFontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;


@Mixin(Font.class)
public class FontRendererMixin {

    @ModifyVariable(
            method = "split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private FormattedText emojiful$prepareTextForWrapping(FormattedText text) {
        return EmojiFontHelper.prepareForWrapping(text);
    }

    @Inject(method = "drawInBatch8xOutline",  at = @At("HEAD"), cancellable = true)
    private void drawInBatch8xOutline(FormattedCharSequence reorderingProcessor, float x, float y, int color, int colorBack, Matrix4f matrix, MultiBufferSource buffer, int packedLight, CallbackInfo ci){
        if (reorderingProcessor != null) {
            EmojiFontHelper.ParsedSequence parsed = EmojiFontHelper.parseSequence(reorderingProcessor);
            if (parsed.sequence() != reorderingProcessor || !parsed.emojis().isEmpty()) {
                color = (color & -67108864) == 0 ? color | -16777216 : color;
                Matrix4f matrix4f = new Matrix4f(matrix);
                EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(parsed.emojis(), buffer, x, y, color, false, matrix4f, false, packedLight);
                parsed.sequence().accept(fontrenderer$characterrenderer);
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F", at = @At("HEAD"), cancellable = true)
    private void renderText(String text, float x, float y, int color, boolean isShadow, Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode displayMode, int colorBackgroundIn, int packedLight, CallbackInfoReturnable<Float> cir) {
        if (text.isEmpty()){
            return;
        }
        HashMap<Integer, Emoji> emojis = new LinkedHashMap<>();
        try {
            Pair<String, HashMap<Integer, Emoji>> cache = EmojiFontHelper.RECENT_STRINGS.get(text);
            text = cache.getLeft();
            emojis = cache.getRight();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (!emojis.isEmpty() || text.startsWith(EmojiFontHelper.SCAPED_STRING)) {
            text = text.replace(EmojiFontHelper.SCAPED_STRING, "");
            EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(emojis, buffer, x, y, color, isShadow, matrix, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
            StringDecomposer.iterateFormatted(text, Style.EMPTY, fontrenderer$characterrenderer);
            cir.setReturnValue(fontrenderer$characterrenderer.finish(colorBackgroundIn, x));
        }

    }


    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    public void drawInBatch(FormattedCharSequence reorderingProcessor, float x, float y, int color, boolean isShadow, Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode displayMode, int colorBackgroundIn, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (reorderingProcessor != null) {
            EmojiFontHelper.ParsedSequence parsed = EmojiFontHelper.parseSequence(reorderingProcessor);
            if (parsed.sequence() != reorderingProcessor || !parsed.emojis().isEmpty()) {
                color = (color & -67108864) == 0 ? color | -16777216 : color;
                Matrix4f matrix4f = new Matrix4f(matrix);

                if (isShadow) {
                    EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(parsed.emojis(), buffer, x, y, color, true, matrix4f, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
                    parsed.sequence().accept(fontrenderer$characterrenderer);
                    fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
                    matrix4f.translate(EmojiFontHelper.SHADOW_OFFSET);
                }
                EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(parsed.emojis(), buffer, x, y, color, false, matrix4f, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
                parsed.sequence().accept(fontrenderer$characterrenderer);
                cir.setReturnValue((int) fontrenderer$characterrenderer.finish(colorBackgroundIn, x));
            }
        }
        //return super.drawInBatch(reorderingProcessor, x, y, color, isShadow, matrix, buffer, displayMode, colorBackgroundIn, packedLight);
    }

    @Inject(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void widthString(String text, CallbackInfoReturnable<Integer> cir){
        if (!EmojiFontHelper.BYPASS.get()) {
            Font thisObject = (Font)(Object)this;
            cir.setReturnValue(EmojiFontHelper.width(thisObject, text));
        }
    }

    @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
    private void widthFormattedCharSeq(FormattedCharSequence processor, CallbackInfoReturnable<Integer> cir){
        if (!EmojiFontHelper.BYPASS.get()) {
            Font thisObject = (Font)(Object)this;
            cir.setReturnValue(EmojiFontHelper.width(thisObject, processor));
        }
    }

    @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
    private void widthFormattedText(FormattedText formattedText, CallbackInfoReturnable<Integer> cir){
        if (!EmojiFontHelper.BYPASS.get()) {
            Font thisObject = (Font)(Object)this;
            cir.setReturnValue(EmojiFontHelper.width(thisObject, formattedText.getString()));
        }
    }
}
