package com.hrznstudio.emojiful.gui;

import com.hrznstudio.emojiful.ClientProxy;
import com.hrznstudio.emojiful.Emojiful;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EmojiSelectionGui implements IDrawableGuiListener  {

    private int selectionPointer;
    private int categoryPointer;
    private ChatScreen chatScreen;
    private int openSelectionAreaEmoji;
    private boolean showingSelectionArea;
    private TextFieldWidget fieldWidget;

    private Rectangle2d openSelectionArea;
    private Rectangle2d selectionArea;
    private Rectangle2d categorySelectionArea;
    private Rectangle2d emojiInfoArea;
    private Rectangle2d textFieldRectangle;

    private double lastMouseX;
    private double lastMouseY;
    private Emoji lastEmoji;
    private List<Emoji[]> filteredEmojis;

    public EmojiSelectionGui(ChatScreen screen) {
        this.selectionPointer = 1;
        this.categoryPointer = 0;
        this.chatScreen = screen;
        this.openSelectionAreaEmoji = -1;
        if (Emojiful.EMOJI_MAP.containsKey("Smileys & Emotion"))this.openSelectionAreaEmoji = new Random().nextInt(Emojiful.EMOJI_MAP.get("Smileys & Emotion").size());
        this.showingSelectionArea = false;
        int offset = 0;
        if (ModList.get().isLoaded("quark")) offset = -80;
        this.openSelectionArea = new Rectangle2d(chatScreen.width - 14, chatScreen.height - 12, 12, 12);
        this.selectionArea = new Rectangle2d(chatScreen.width - 14 - 11*12 + offset , chatScreen.height - 16 - 10*11 - 4, 11*12 + 4, 10*11 + 4);
        this.categorySelectionArea = new Rectangle2d(this.selectionArea.getX(), this.selectionArea.getY() + 20, 22, this.selectionArea.getHeight() - 20);
        this.emojiInfoArea = new Rectangle2d(this.selectionArea.getX() + 22, this.selectionArea.getY() + this.selectionArea.getHeight() - 20,  this.selectionArea.getWidth() - 22,  20);
        this.textFieldRectangle = new Rectangle2d(selectionArea.getX() + 6, selectionArea.getY() + 6, selectionArea.getWidth() -12, 10);
        this.fieldWidget = new TextFieldWidget(ClientProxy.oldFontRenderer, textFieldRectangle.getX(), textFieldRectangle.getY(), textFieldRectangle.getWidth(), textFieldRectangle.getHeight(), new StringTextComponent("") );
        this.fieldWidget.setEnabled(true);
        this.fieldWidget.setVisible(true);
        this.filteredEmojis = new ArrayList<>();
    }

    @Override
    public void render(MatrixStack stack) {
        if (this.openSelectionAreaEmoji != -1)Minecraft.getInstance().fontRenderer.drawString(stack, Emojiful.EMOJI_MAP.get("Smileys & Emotion").get(openSelectionAreaEmoji).strings.get(0), openSelectionArea.getX(), openSelectionArea.getY(), 0);
        if (this.showingSelectionArea){
            drawRectangle(stack, this.selectionArea);
            drawRectangle(stack, this.categorySelectionArea);
            drawRectangle(stack, this.emojiInfoArea);
            for (int i = 0; i < 6; i++) {
                drawLine(stack, i * 12f, i + selectionPointer);
            }
            int progressY = (int) ((( this.emojiInfoArea.getY() - this.categorySelectionArea.getY() - 5) / ((double)getLineAmount())) * (selectionPointer)) ;
            drawRectangle(stack, new Rectangle2d(this.selectionArea.getX() + this.selectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY, 1,5), 0xff525252);
            if (lastEmoji != null){
                Minecraft.getInstance().fontRenderer.drawString(stack, lastEmoji.strings.get(0), emojiInfoArea.getX() + 2, emojiInfoArea.getY() + 6, 0);
                StringBuilder builder = new StringBuilder();
                lastEmoji.strings.forEach(s -> builder.append(s).append(" "));
                float textScale = 0.5f;
                List<IReorderingProcessor> iTextPropertiesList = ClientProxy.oldFontRenderer.trimStringToWidth(new StringTextComponent(builder.toString()), (int) ((emojiInfoArea.getWidth() - 18) *  (1/textScale)));
                float i = -iTextPropertiesList.size() / 2;
                stack.push();
                stack.scale(textScale, textScale, textScale);
                for (IReorderingProcessor reorderingProcessor : iTextPropertiesList) {
                    StringBuilder stringBuilder = new StringBuilder();
                    reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                        stringBuilder.append((char) ch);
                        return true;
                    });
                    ClientProxy.oldFontRenderer.drawString(stack, stringBuilder.toString(), (emojiInfoArea.getX() + 15) * (1/textScale), (emojiInfoArea.getY() + 8 + 4 * i)  * (1/textScale), 0x969696);
                    ++i;
                }
                stack.scale(1,1,1);
                stack.pop();
            }
            progressY = (int) ((( this.categorySelectionArea.getHeight() - 10) / ((double)ClientProxy.CATEGORIES.size() -7)) * (categoryPointer)) ;
            drawRectangle(stack, new Rectangle2d(this.categorySelectionArea.getX() + this.categorySelectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY + 2, 1,5), 0xff525252);
            EmojiCategory firstCategory = getCategory(selectionPointer);
            for (int i = 0; i < 7; i++) {
                int selCategory = i + categoryPointer;
                if (selCategory < ClientProxy.CATEGORIES.size()){
                    EmojiCategory category = ClientProxy.CATEGORIES.get(selCategory);
                    Rectangle2d rec = new Rectangle2d(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                    if (category.equals(firstCategory)){
                        AbstractGui.fill(stack, rec.getX()-1, rec.getY()-2, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight() -1, -2130706433);
                    }
                    if (rec.contains((int)lastMouseX, (int)lastMouseY) && Minecraft.getInstance().currentScreen != null){
                        Minecraft.getInstance().currentScreen.func_243308_b(stack, Arrays.asList(new StringTextComponent(category.getName())),(int) lastMouseX,(int) lastMouseY);
                    }
                    if (ClientProxy.SORTED_EMOJIS_FOR_SELECTION.containsKey(category) && ClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category).size() > 0){
                        Minecraft.getInstance().fontRenderer.drawString(stack, ClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category).get(0)[0].strings.get(0), categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 0);
                    }
                }
            }
            fieldWidget.render(stack, (int)lastMouseX, (int)lastMouseY, 0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_) {
        if (this.showingSelectionArea){
            if (textFieldRectangle.contains((int)mouseX, (int)mouseY)){
                fieldWidget.setFocused2(true);
            } else {
                fieldWidget.setFocused2(false);
            }
            if (categorySelectionArea.contains((int)mouseX, (int)mouseY)){
                for (int i = 0; i < 7; i++) {
                    int selCategory = i + categoryPointer;
                    if (selCategory < ClientProxy.CATEGORIES.size()){
                        Rectangle2d rec = new Rectangle2d(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                        if (rec.contains((int)mouseX, (int)mouseY)){
                            EmojiCategory name = ClientProxy.CATEGORIES.get(selCategory);
                            for (int i1 = 0; i1 < getLineAmount(); i1++) {
                                if (name.equals(getLineToDraw(i1))){
                                    this.selectionPointer = i1;
                                }
                            }
                        }
                    }
                }
                return true;
            }
            if (selectionArea.contains((int)mouseX, (int)mouseY)){
                for (int line = 0; line < 6; line++) {
                    Object object = getLineToDraw(line + selectionPointer);
                    if (object instanceof Emoji[]){
                        Emoji[] emojis = (Emoji[]) object;
                        for (int i = 0; i < emojis.length; i++) {
                            if (emojis[i] != null){
                                float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                                float y = (categorySelectionArea.getY() + line * 12 + 2);//
                                Rectangle2d rec = new Rectangle2d((int) x, (int) y -1, 11, 11);
                                if (rec.contains((int)lastMouseX, (int)lastMouseY)){
                                    chatScreen.inputField.setText(chatScreen.inputField.getText() + emojis[i].getShorterString());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        } else {
            if (openSelectionArea.contains((int)mouseX, (int)mouseY)){
                showSelectionArea();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (categorySelectionArea.contains((int)mouseX, (int)mouseY)){
            categoryPointer -= delta;
            categoryPointer = MathHelper.clamp(categoryPointer, 0, ClientProxy.CATEGORIES.size() - 7);
            return true;
        }
        if (selectionArea.contains((int)mouseX, (int)mouseY)){
            selectionPointer -= delta;
            selectionPointer = MathHelper.clamp(selectionPointer, 1, Math.max(1, getLineAmount() - 5));
            categoryPointer = MathHelper.clamp(Arrays.asList(ClientProxy.CATEGORIES).indexOf(getCategory(selectionPointer)), 0, ClientProxy.CATEGORIES.size() - 7);
            return true;
        }
        return false;
    }


    public void drawRectangle(MatrixStack stack, Rectangle2d rectangle2d){
        drawRectangle(stack, rectangle2d, Integer.MIN_VALUE);
    }

    public void drawRectangle(MatrixStack stack, Rectangle2d rectangle2d, int value){
        AbstractGui.fill(stack, rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getX() + rectangle2d.getWidth(), rectangle2d.getY() + rectangle2d.getHeight(), value);
    }

    public void showSelectionArea(){
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        showingSelectionArea = !showingSelectionArea;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fieldWidget.keyPressed(keyCode, scanCode, modifiers)){
            updateFilter();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if (fieldWidget.charTyped(c, mod)){
            updateFilter();
            return true;
        }
        return false;
    }

    public void drawLine(MatrixStack stack, float height, int line){
        Object lineToDraw = getLineToDraw(line);
        if (lineToDraw != null){
            if (lineToDraw instanceof EmojiCategory){
                float textScale = 1f;
                RenderSystem.scaled(textScale, textScale, textScale);
                Minecraft.getInstance().fontRenderer.drawString(stack, ((EmojiCategory) lineToDraw).getName(), (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2) * (1/textScale), (categorySelectionArea.getY() + height + 2)* (1/textScale), 0x969696);
                RenderSystem.scaled(1,1,1);
            } else {
                Emoji[] emojis = (Emoji[]) lineToDraw;
                for (int i = 0; i < emojis.length; i++) {
                    if (emojis[i] != null){
                        float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                        float y = (categorySelectionArea.getY() + height + 2);//
                        Rectangle2d rec = new Rectangle2d((int) x, (int) y -1, 11, 11);
                        if (rec.contains((int)lastMouseX, (int)lastMouseY)){
                            lastEmoji = emojis[i];
                            AbstractGui.fill(stack, rec.getX()-1, rec.getY()-1, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight(), -2130706433);
                        }
                        Minecraft.getInstance().fontRenderer.drawString(stack, emojis[i].strings.get(0), x, y, 0x969696);
                    }
                }
            }
        }
    }

    public Object getLineToDraw(int line){
        if (fieldWidget.getText().isEmpty()){
            for (EmojiCategory category : ClientProxy.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
                --line;
                if (line == 0) return category;
                for (Emoji[] emojis : ClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
                    --line;
                    if (line == 0) return emojis;
                }
            }
        } else {
            if (filteredEmojis.size() > line - 1 && line -1  >= 0){
                return filteredEmojis.get(line -1);
            }
        }
        return null;
    }

    public void updateFilter(){
        if (!fieldWidget.getText().isEmpty()){
            selectionPointer = 1;
            filteredEmojis = new ArrayList<>();
            List<Emoji> emojis = Emojiful.EMOJI_LIST.stream().filter(emoji -> emoji.strings.stream().anyMatch(s -> s.toLowerCase().contains(fieldWidget.getText().toLowerCase()))).collect(Collectors.toList());
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : emojis) {
                array[i] = emoji;
                ++i;
                if (i >= array.length){
                    filteredEmojis.add(array);
                    array = new Emoji[9];
                    i = 0;
                }
            }
            if (i > 0){
                filteredEmojis.add(array);
            }
        }
    }

    public int getLineAmount(){
        return fieldWidget.getText().isEmpty() ? ClientProxy.lineAmount : filteredEmojis.size();
    }

    public EmojiCategory getCategory(int line){
        for (EmojiCategory category : ClientProxy.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
            --line;
            if (line == 0) return category;
            for (Emoji[] emojis : ClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
                --line;
                if (line == 0) return category;
            }
        }
        return null;
    }

    public ChatScreen getChatScreen() {
        return chatScreen;
    }

    public TextFieldWidget getFieldWidget() {
        return fieldWidget;
    }
}
