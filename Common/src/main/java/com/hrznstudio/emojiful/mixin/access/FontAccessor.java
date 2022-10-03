package com.hrznstudio.emojiful.mixin.access;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(Font.class)
public interface FontAccessor {
    @Invoker("renderText")
    float emojifulCommon_renderText(String string, float i, float j, int k, boolean b1, Matrix4f matrix4f, MultiBufferSource source, boolean b2, int l, int m);
    @Invoker("renderChar")
    void emojifulCommon_renderChar(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j, float k, float l, int m);
    @Invoker("getFontSet")
    FontSet emojifulCommon_getFontSet(ResourceLocation resourceLocation);
    @Accessor("filterFishyGlyphs")
    boolean emojifulCommon_getFishyGlyphs();
    @Accessor("fonts")
    Function<ResourceLocation, FontSet> emojifulCommon_getFonts();

}
