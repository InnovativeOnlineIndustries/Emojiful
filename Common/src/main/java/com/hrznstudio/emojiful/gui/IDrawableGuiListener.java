package com.hrznstudio.emojiful.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class IDrawableGuiListener implements GuiEventListener {


    abstract void render(GuiGraphics guiGraphics);

    @Override
    public void setFocused(boolean b) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
