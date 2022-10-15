package com.hrznstudio.emojiful.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IDrawableGuiListener extends GuiEventListener {

    void render(PoseStack stack);

}
