package com.hrznstudio.emojiful.gui;

import com.hrznstudio.emojiful.CommonClass;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

public class EmojifulChatScreen extends ChatScreen {

    private EmojiSelectionGui emojiSelectionGui;
    private EmojiSuggestionHelper emojiSuggestionHelper;

    public EmojifulChatScreen(String initial) {
        super(initial, false);
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {
        super.extractRenderState(graphics, x, y, partialTick);
        if (emojiSuggestionHelper != null) emojiSuggestionHelper.extractRenderState(graphics, x, y, partialTick);
        if (emojiSelectionGui != null) {
            emojiSelectionGui.mouseMoved(x, y);
            emojiSelectionGui.extractRenderState(graphics, x, y, partialTick);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(event))
            return true;
        if (emojiSelectionGui != null && emojiSelectionGui.keyPressed(event)){
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollDelta, double d) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseScrolled(x, y, scrollDelta, d)) return true;
        return super.mouseScrolled(x, y, scrollDelta, d);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseClicked(event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (emojiSelectionGui != null && emojiSelectionGui.charTyped(event)){
            return true;
        }
        return super.charTyped(event);
    }
}
