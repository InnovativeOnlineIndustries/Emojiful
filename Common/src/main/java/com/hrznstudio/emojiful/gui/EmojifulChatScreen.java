package com.hrznstudio.emojiful.gui;

import com.hrznstudio.emojiful.CommonClass;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

public class EmojifulChatScreen extends ChatScreen {

    private EmojiSelectionGui emojiSelectionGui;
    private EmojiSuggestionHelper emojiSuggestionHelper;

    public EmojifulChatScreen(String initial) {
        super(initial);
    }

    @Override
    protected void init() {
        super.init();
        if (!Constants.error) {
            if (Services.CONFIG.showEmojiAutocomplete()) emojiSuggestionHelper = new EmojiSuggestionHelper(this);
            if (Services.CONFIG.showEmojiSelector()) emojiSelectionGui = new EmojiSelectionGui(this);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int x, int j, float partialTick) {
        super.render(guiGraphics, x, j, partialTick);
        if (emojiSuggestionHelper != null) emojiSuggestionHelper.render(guiGraphics);
        if (emojiSelectionGui != null) {
            emojiSelectionGui.mouseMoved(x, j);
            emojiSelectionGui.render(guiGraphics);
        }

    }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers) && !CommonClass.shouldKeyBeIgnored(keyCode)){
            return true;
        }
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return emojiSelectionGui != null && emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollDelta) {
        return super.mouseScrolled(x, y, scrollDelta) && (emojiSelectionGui != null) && emojiSelectionGui.mouseScrolled(x, y, scrollDelta);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (emojiSelectionGui != null) emojiSelectionGui.mouseClicked(x, y, button);
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean charTyped(char c, int i) {
        return super.charTyped(c, i) && (emojiSelectionGui != null && emojiSelectionGui.charTyped(c, i));
    }
}
