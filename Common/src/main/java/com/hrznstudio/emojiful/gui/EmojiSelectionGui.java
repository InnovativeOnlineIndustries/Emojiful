package com.hrznstudio.emojiful.gui;

import com.hrznstudio.emojiful.ClientEmojiHandler;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.render.EmojiFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.stream.Collectors;

public class EmojiSelectionGui extends IDrawableGuiListener {
    private static final int EMOJI_COLOR = 0xFFFFFFFF;
    private static final int LABEL_COLOR = 0xFF969696;

    private final ChatScreen chatScreen;
    private final EditBox fieldWidget;
    private final Rect2i openSelectionArea;
    private final Rect2i selectionArea;
    private final Rect2i categorySelectionArea;
    private final Rect2i emojiInfoArea;
    private final Rect2i textFieldRectangle;
    private int selectionPointer;
    private int categoryPointer;
    private int openSelectionAreaEmoji;
    private boolean showingSelectionArea;
    private double lastMouseX;
    private double lastMouseY;
    private Emoji lastEmoji;
    private List<Emoji[]> filteredEmojis;

    public EmojiSelectionGui(ChatScreen screen) {
        this.selectionPointer = 1;
        this.categoryPointer = 0;
        this.chatScreen = screen;
        this.openSelectionAreaEmoji = -1;
        if (Constants.EMOJI_MAP.containsKey("Smileys & Emotion"))
            this.openSelectionAreaEmoji = new Random().nextInt(Constants.EMOJI_MAP.get("Smileys & Emotion").size());
        this.showingSelectionArea = false;
        int offset = 0;
        if (Services.PLATFORM.isModLoaded("quark")) offset = -80;
        this.openSelectionArea = new Rect2i(chatScreen.width - 14, chatScreen.height - 12, 12, 12);
        this.selectionArea = new Rect2i(chatScreen.width - 14 - 11 * 12 + offset, chatScreen.height - 16 - 10 * 11 - 4, 11 * 12 + 4, 10 * 11 + 4);
        this.categorySelectionArea = new Rect2i(this.selectionArea.getX(), this.selectionArea.getY() + 20, 22, this.selectionArea.getHeight() - 20);
        this.emojiInfoArea = new Rect2i(this.selectionArea.getX() + 22, this.selectionArea.getY() + this.selectionArea.getHeight() - 20, this.selectionArea.getWidth() - 22, 20);
        this.textFieldRectangle = new Rect2i(selectionArea.getX() + 6, selectionArea.getY() + 6, selectionArea.getWidth() - 12, 10);
        this.fieldWidget = new EditBox(Minecraft.getInstance().font, textFieldRectangle.getX(), textFieldRectangle.getY(), textFieldRectangle.getWidth(), textFieldRectangle.getHeight(), MutableComponent.create(new PlainTextContents.LiteralContents("")));
        this.fieldWidget.setEditable(true);
        this.fieldWidget.setVisible(true);
        this.filteredEmojis = new ArrayList<>();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.openSelectionAreaEmoji == -1) {
            List<Emoji> smileys = Constants.EMOJI_MAP.get("Smileys & Emotion");
            if (smileys != null && !smileys.isEmpty()) {
                this.openSelectionAreaEmoji = new Random().nextInt(smileys.size());
            }
        }
        if (this.openSelectionAreaEmoji != -1)
            guiGraphics.text(Minecraft.getInstance().font, Constants.EMOJI_MAP.get("Smileys & Emotion").get(openSelectionAreaEmoji).strings.get(0), openSelectionArea.getX(), openSelectionArea.getY(), EMOJI_COLOR);
        if (this.showingSelectionArea) {
            drawRectangle(guiGraphics, this.selectionArea);
            drawRectangle(guiGraphics, this.categorySelectionArea);
            drawRectangle(guiGraphics, this.emojiInfoArea);
            for (int i = 0; i < 6; i++) {
                drawLine(guiGraphics, i * 12f, i + selectionPointer);
            }
            int progressY = (int) (((this.emojiInfoArea.getY() - this.categorySelectionArea.getY() - 5) / ((double) getLineAmount())) * (selectionPointer));
            drawRectangle(guiGraphics, new Rect2i(this.selectionArea.getX() + this.selectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY, 1, 5), 0xff525252);
            if (lastEmoji != null) {
                guiGraphics.text(Minecraft.getInstance().font, lastEmoji.strings.get(0), emojiInfoArea.getX() + 2, emojiInfoArea.getY() + 6, EMOJI_COLOR);
                StringBuilder builder = new StringBuilder();
                lastEmoji.strings.forEach(s -> builder.append(s).append(" "));
                List<FormattedCharSequence> iTextPropertiesList = Minecraft.getInstance().font.split(FormattedText.of(builder.toString()), (emojiInfoArea.getWidth() - 18) * 2);
                float i = -iTextPropertiesList.size() / 2;
                for (FormattedCharSequence reorderingProcessor : iTextPropertiesList) {
                    StringBuilder stringBuilder = new StringBuilder();
                    reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                        stringBuilder.append((char) ch);
                        return true;
                    });
                    guiGraphics.text(Minecraft.getInstance().font, EmojiFontHelper.SCAPED_STRING + stringBuilder, emojiInfoArea.getX() + 15, (int) (emojiInfoArea.getY() + 8 + 4 * i), LABEL_COLOR);
                    ++i;
                }
            }
            progressY = (int) (((this.categorySelectionArea.getHeight() - 10) / ((double) ClientEmojiHandler.CATEGORIES.size() - 7)) * (categoryPointer));
            drawRectangle(guiGraphics, new Rect2i(this.categorySelectionArea.getX() + this.categorySelectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY + 2, 1, 5), 0xff525252);
            EmojiCategory firstCategory = getCategory(selectionPointer);
            for (int i = 0; i < 7; i++) {
                int selCategory = i + categoryPointer;
                if (selCategory < ClientEmojiHandler.CATEGORIES.size()) {
                    EmojiCategory category = ClientEmojiHandler.CATEGORIES.get(selCategory);
                    Rect2i rec = new Rect2i(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                    if (category.equals(firstCategory)) {
                        guiGraphics.fill(rec.getX() - 1, rec.getY() - 2, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight() - 1, -2130706433);
                    }
                    if (rec.contains((int) lastMouseX, (int) lastMouseY) && Minecraft.getInstance().screen != null) {
                        guiGraphics.setTooltipForNextFrame(MutableComponent.create(new PlainTextContents.LiteralContents(category.name())), (int) lastMouseX, (int) lastMouseY);
                    }
                    if (ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.containsKey(category) && ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.get(category).size() > 0) {
                        guiGraphics.text(Minecraft.getInstance().font, ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.get(category).get(0)[0].strings.get(0), categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, EMOJI_COLOR);
                    }
                }
            }
            fieldWidget.extractRenderState(guiGraphics, (int) lastMouseX, (int) lastMouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (openSelectionArea.contains((int) mouseX, (int) mouseY)) {
            toggleSelectionArea();
            return true;
        }

        if (this.showingSelectionArea) {
            fieldWidget.setFocused(textFieldRectangle.contains((int) mouseX, (int) mouseY));
            if (categorySelectionArea.contains((int) mouseX, (int) mouseY)) {
                for (int i = 0; i < 7; i++) {
                    int selCategory = i + categoryPointer;
                    if (selCategory < ClientEmojiHandler.CATEGORIES.size()) {
                        Rect2i rec = new Rect2i(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                        if (rec.contains((int) mouseX, (int) mouseY)) {
                            EmojiCategory name = ClientEmojiHandler.CATEGORIES.get(selCategory);
                            for (int i1 = 0; i1 < getLineAmount(); i1++) {
                                if (name.equals(getLineToDraw(i1))) {
                                    this.selectionPointer = i1;
                                }
                            }
                        }
                    }
                }
                return true;
            }
            if (selectionArea.contains((int) mouseX, (int) mouseY)) {
                for (int line = 0; line < 6; line++) {
                    Object object = getLineToDraw(line + selectionPointer);
                    if (object instanceof Emoji[]) {
                        Emoji[] emojis = (Emoji[]) object;
                        for (int i = 0; i < emojis.length; i++) {
                            if (emojis[i] != null) {
                                float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                                float y = (categorySelectionArea.getY() + line * 12 + 2);//
                                Rect2i rec = new Rect2i((int) x, (int) y - 1, 11, 11);
                                if (rec.contains((int) lastMouseX, (int) lastMouseY)) {
                                    chatScreen.input.setValue(chatScreen.input.getValue() + emojis[i].getShorterString());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double d) {
        if (categorySelectionArea.contains((int) mouseX, (int) mouseY)) {
            categoryPointer -= d;
            categoryPointer = Mth.clamp(categoryPointer, 0, ClientEmojiHandler.CATEGORIES.size() - 7);
            return true;
        }
        if (selectionArea.contains((int) mouseX, (int) mouseY)) {
            selectionPointer -= d;
            selectionPointer = Mth.clamp(selectionPointer, 1, Math.max(1, getLineAmount() - 5));
            categoryPointer = Mth.clamp(Arrays.asList(ClientEmojiHandler.CATEGORIES).indexOf(getCategory(selectionPointer)), 0, ClientEmojiHandler.CATEGORIES.size() - 7);
            return true;
        }
        return false;
    }


    public void drawRectangle(GuiGraphicsExtractor guiGraphics, Rect2i rectangle2d) {
        drawRectangle(guiGraphics, rectangle2d, Integer.MIN_VALUE);
    }

    public void drawRectangle(GuiGraphicsExtractor guiGraphics, Rect2i rectangle2d, int value) {
        guiGraphics.fill(rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getX() + rectangle2d.getWidth(), rectangle2d.getY() + rectangle2d.getHeight(), value);
    }

    public void toggleSelectionArea() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        showingSelectionArea = !showingSelectionArea;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (fieldWidget.keyPressed(event)) {
            updateFilter();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (fieldWidget.charTyped(event)) {
            updateFilter();
            return true;
        }
        return false;
    }

    public void drawLine(GuiGraphicsExtractor guiGraphics, float height, int line) {
        Object lineToDraw = getLineToDraw(line);
        if (lineToDraw != null) {
            if (lineToDraw instanceof EmojiCategory) {
                guiGraphics.text(Minecraft.getInstance().font, ((EmojiCategory) lineToDraw).name(), categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2, (int) (categorySelectionArea.getY() + height + 2), LABEL_COLOR);
            } else {
                Emoji[] emojis = (Emoji[]) lineToDraw;
                for (int i = 0; i < emojis.length; i++) {
                    if (emojis[i] != null) {
                        float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                        float y = (categorySelectionArea.getY() + height + 2);//
                        Rect2i rec = new Rect2i((int) x, (int) y - 1, 11, 11);
                        if (rec.contains((int) lastMouseX, (int) lastMouseY)) {
                            lastEmoji = emojis[i];
                            guiGraphics.fill(rec.getX() - 1, rec.getY() - 1, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight(), -2130706433);
                        }
                        guiGraphics.text(Minecraft.getInstance().font, emojis[i].strings.get(0), (int) x, (int) y, EMOJI_COLOR);
                    }
                }
            }
        }
    }

    public Object getLineToDraw(int line) {
        if (fieldWidget.getValue().isEmpty()) {
            for (EmojiCategory category : ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
                --line;
                if (line == 0) return category;
                for (Emoji[] emojis : ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
                    --line;
                    if (line == 0) return emojis;
                }
            }
        } else {
            if (filteredEmojis.size() > line - 1 && line - 1 >= 0) {
                return filteredEmojis.get(line - 1);
            }
        }
        return null;
    }

    public void updateFilter() {
        if (!fieldWidget.getValue().isEmpty()) {
            selectionPointer = 1;
            filteredEmojis = new ArrayList<>();
            if (!ClientEmojiHandler.areEmojisLoaded()) {
                return;
            }
            List<Emoji> emojis = Constants.EMOJI_LIST.stream().filter(emoji -> emoji.strings.stream().anyMatch(s -> s.toLowerCase().contains(fieldWidget.getValue().toLowerCase()))).collect(Collectors.toList());
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : emojis) {
                array[i] = emoji;
                ++i;
                if (i >= array.length) {
                    filteredEmojis.add(array);
                    array = new Emoji[9];
                    i = 0;
                }
            }
            if (i > 0) {
                filteredEmojis.add(array);
            }
        }
    }

    public int getLineAmount() {
        return fieldWidget.getValue().isEmpty() ? ClientEmojiHandler.lineAmount : filteredEmojis.size();
    }

    public EmojiCategory getCategory(int line) {
        for (EmojiCategory category : ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
            --line;
            if (line == 0) return category;
            for (Emoji[] emojis : ClientEmojiHandler.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
                --line;
                if (line == 0) return category;
            }
        }
        return null;
    }

    public ChatScreen getChatScreen() {
        return chatScreen;
    }

    public EditBox getFieldWidget() {
        return fieldWidget;
    }
}
