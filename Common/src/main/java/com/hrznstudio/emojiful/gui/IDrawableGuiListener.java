package com.hrznstudio.emojiful.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class IDrawableGuiListener implements GuiEventListener {
    abstract void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
