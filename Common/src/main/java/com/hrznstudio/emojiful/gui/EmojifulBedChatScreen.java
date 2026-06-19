package com.hrznstudio.emojiful.gui;


import com.hrznstudio.emojiful.CommonClass;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.lwjgl.glfw.GLFW;

public class EmojifulBedChatScreen extends InBedChatScreen {

    private EmojiSelectionGui emojiSelectionGui;
    private EmojiSuggestionHelper emojiSuggestionHelper;

    public EmojifulBedChatScreen() {
        super("", false);
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
        if (super.keyPressed(event) && CommonClass.shouldKeyBeIgnored(event.key())){
            return true;
        }
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(event))
            return true;
        return emojiSelectionGui != null && emojiSelectionGui.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollDelta, double d) {
        return super.mouseScrolled(x, y, scrollDelta, d) && (emojiSelectionGui != null) && emojiSelectionGui.mouseScrolled(x, y, scrollDelta, d);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseClicked(event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return (emojiSelectionGui != null && emojiSelectionGui.charTyped(event)) || super.charTyped(event);
    }

}
