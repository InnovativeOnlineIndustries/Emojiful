package com.hrznstudio.emojiful.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class EmojiButton extends Button {

    private int page;
    public EmojiButton(int widthIn, int heightIn, int width, int height, String text, IPressable onPress, int page) {
        super(widthIn, heightIn, width, height, new StringTextComponent(text), onPress);
        this.page = page;
    }

    @Override
    public void renderButton(MatrixStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        AbstractGui.fill(stack, x, y, x + width, y + height, Integer.MIN_VALUE);
        int j = getFGColor();
        this.drawCenteredString(stack, fontrenderer, this.getMessage(), this.x + this.width / 2 , this.y + (this.height - 8) / 2 - 4, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.5f, 0.5f, 0.5f);
        this.drawCenteredString(stack, fontrenderer, new StringTextComponent((this.isHovered() ? TextFormatting.YELLOW : "") + this.getMessage().getString() + "\u00a7" +"-"), (this.x + this.width / 2) * 2 , (this.y + (this.height - 8) / 2 + 8)*2 , j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.scaled(1,1,1);
        RenderSystem.popMatrix();
    }

    public int getPage() {
        return page;
    }
}
