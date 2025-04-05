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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Mixin(Font.class)
public class FontRendererMixin {

    @Inject(method = "drawInBatch8xOutline",  at = @At("HEAD"), cancellable = true)
    private void drawInBatch8xOutline(FormattedCharSequence reorderingProcessor, float x, float y, int color, int colorBack, Matrix4f matrix, MultiBufferSource buffer, int packedLight, CallbackInfo ci){
        if (reorderingProcessor != null) {
            StringBuilder builder = new StringBuilder();
            if (reorderingProcessor != null) {
                reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                    builder.append((char) ch);
                    return true;
                });
            }
            String text = builder.toString();
            if (text.length() > 0) {
                color = (color & -67108864) == 0 ? color | -16777216 : color;
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
                    List<FormattedCharSequence> processors = new ArrayList<>();
                    HashMap<Integer, Emoji> finalEmojis = emojis;
                    AtomicInteger cleanPos = new AtomicInteger();
                    AtomicBoolean ignore = new AtomicBoolean(false);
                    reorderingProcessor.accept((pos, style, ch) -> {
                        if (!ignore.get()) {
                            if (finalEmojis.get(cleanPos.get()) == null) {
                                processors.add(new EmojiFontHelper.CharacterProcessor(cleanPos.getAndIncrement(), style, ch));
                            } else {
                                processors.add(new EmojiFontHelper.CharacterProcessor(cleanPos.get(), style, ' '));
                                ignore.set(true);
                                return true;
                            }
                        }
                        if (ignore.get() && ch == ':') {
                            ignore.set(false);
                            cleanPos.getAndIncrement();
                        }
                        return true;
                    });
                    Matrix4f matrix4f = new Matrix4f(matrix);

                    /*if (isShadow) {
                        EmojiFontRenderer.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontRenderer.EmojiCharacterRenderer(emojis, buffer, x, y, color, true, matrix4f, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
                        FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                        fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
                        matrix4f.translate(EmojiFontRenderer.SHADOW_OFFSET);
                    }*/
                    EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(emojis, buffer, x, y, color, false, matrix4f, false, packedLight);
                    FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                    ci.cancel();
                }
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
            StringBuilder builder = new StringBuilder();
            if (reorderingProcessor != null) {
                reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                    builder.append((char) ch);
                    return true;
                });
            }
            String text = builder.toString();
            if (text.length() > 0) {
                color = (color & -67108864) == 0 ? color | -16777216 : color;
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
                    List<FormattedCharSequence> processors = new ArrayList<>();
                    HashMap<Integer, Emoji> finalEmojis = emojis;
                    AtomicInteger cleanPos = new AtomicInteger();
                    AtomicBoolean ignore = new AtomicBoolean(false);
                    reorderingProcessor.accept((pos, style, ch) -> {
                        if (!ignore.get()) {
                            if (finalEmojis.get(cleanPos.get()) == null) {
                                processors.add(new EmojiFontHelper.CharacterProcessor(cleanPos.getAndIncrement(), style, ch));
                            } else {
                                processors.add(new EmojiFontHelper.CharacterProcessor(cleanPos.get(), style, ' '));
                                ignore.set(true);
                                return true;
                            }
                        }
                        if (ignore.get() && ch == ':') {
                            ignore.set(false);
                            cleanPos.getAndIncrement();
                        }
                        return true;
                    });
                    Matrix4f matrix4f = new Matrix4f(matrix);

                    if (isShadow) {
                        EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(emojis, buffer, x, y, color, true, matrix4f, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
                        FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                        fontrenderer$characterrenderer.finish(colorBackgroundIn, x);
                        matrix4f.translate(EmojiFontHelper.SHADOW_OFFSET);
                    }
                    EmojiFontHelper.EmojiCharacterRenderer fontrenderer$characterrenderer = new EmojiFontHelper.EmojiCharacterRenderer(emojis, buffer, x, y, color, false, matrix4f, displayMode == Font.DisplayMode.SEE_THROUGH, packedLight);
                    FormattedCharSequence.fromList(processors).accept(fontrenderer$characterrenderer);
                    cir.setReturnValue((int) fontrenderer$characterrenderer.finish(colorBackgroundIn, x));
                }
            }
        }
        //return super.drawInBatch(reorderingProcessor, x, y, color, isShadow, matrix, buffer, displayMode, colorBackgroundIn, packedLight);
    }

    @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
    private void widthFormattedCharSeq(FormattedCharSequence processor, CallbackInfoReturnable<Integer> cir){
        StringBuilder builder = new StringBuilder();
        processor.accept((p_accept_1_, p_accept_2_, ch) -> {
            builder.append((char) ch);
            return true;
        });
        Font thisObject = (Font)(Object)this;
        cir.setReturnValue(thisObject.width(builder.toString()));
    }

    @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
    private void widthFormattedText(FormattedText formattedText, CallbackInfoReturnable<Integer> cir){
        Font thisObject = (Font)(Object)this;
        cir.setReturnValue(thisObject.width(formattedText.getString()));
    }

    @ModifyArg(method = "width(Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Ljava/lang/String;)F"), index = 0)
    private String injectedWidth(String value) {
        if (value != null) {
            try {
                value = EmojiFontHelper.RECENT_STRINGS.get(value).getKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }
}
