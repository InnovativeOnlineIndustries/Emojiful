package com.hrznstudio.emojiful.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;

public interface IDrawableGuiListener extends IGuiEventListener {

    void render(MatrixStack stack);

}
