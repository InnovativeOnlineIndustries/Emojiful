package com.hrznstudio.emojiful.gui;

import com.google.common.base.Strings;
import com.hrznstudio.emojiful.ClientProxy;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiSuggestionHelper implements IDrawableGuiListener {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    private final ChatScreen chatScreen;
    private CompletableFuture<Suggestions> suggestionsFuture;
    private EmojiSuggestions suggestions;
    private boolean updating;
    private String inputFieldTextLast;

    public EmojiSuggestionHelper(ChatScreen screen) {
        this.chatScreen = screen;
        this.updating = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.suggestions != null && this.suggestions.onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 258) {
            this.updateSuggestionList(false);
            return suggestions != null;
        } else {
            return false;
        }
    }

    public void updateSuggestionList(boolean skip) {
        final String s = this.chatScreen.inputField.getText();

        if (!this.updating) {
            this.chatScreen.inputField.setSuggestion(null);
            this.suggestions = null;
        }

        final StringReader stringreader = new StringReader(s);
        if (stringreader.canRead()) {
            final int cursorPosition = this.chatScreen.inputField.getCursorPosition();
            final int lastWordIndex = getLastWordIndex(s);
            if (lastWordIndex < s.length() ? s.charAt(lastWordIndex) == ':' : s.length() > 0 && s.charAt(0) == ':')
                if ((skip || cursorPosition - lastWordIndex >= 3) && (this.suggestions == null || !this.updating)) {
                    final CompletableFuture<Iterable<String>> list = CompletableFuture.supplyAsync(() -> ClientProxy.ALL_EMOJIS);
                    this.suggestionsFuture = list.thenApplyAsync(stringIterable -> createSuggestions(stringIterable, new SuggestionsBuilder(s, lastWordIndex)));
                    this.suggestionsFuture.thenRun(() -> {
                        if (this.suggestionsFuture.isDone())
                            showSuggestions();
                    });
                }
        }
    }

    public void showSuggestions() {
        if (this.suggestionsFuture != null && this.suggestionsFuture.isDone()) {
            int i = 0;
            final Suggestions suggestions = this.suggestionsFuture.join();
            if (!suggestions.getList().isEmpty()) {
                for (final Suggestion suggestion : suggestions.getList())
                    i = Math.max(i, ClientProxy.oldFontRenderer.getStringWidth(suggestion.getText()));
                final int j = Minecraft.getInstance().fontRenderer.getStringWidth(this.chatScreen.inputField.getText().substring(0, this.chatScreen.inputField.getCursorPosition() - suggestions.getRange().getLength() + 2));
                this.suggestions = new EmojiSuggestions(j, this.chatScreen.height - 12, i, suggestions);
            }
        }
    }

    private static Suggestions createSuggestions(final Iterable<String> collection, final SuggestionsBuilder suggestionBuilder) {
        final String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String key : collection){
            if (key.toLowerCase(Locale.ROOT).startsWith(remaining))
                suggestionBuilder.suggest(key);
        }
        return suggestionBuilder.build();
    }

    private static int getLastWordIndex(String p_228121_0_) {
        if (Strings.isNullOrEmpty(p_228121_0_)) {
            return 0;
        } else {
            int i = 0;

            for(Matcher matcher = WHITESPACE_PATTERN.matcher(p_228121_0_); matcher.find(); i = matcher.end()) {

            }

            return i;
        }
    }

    private static String trim(String text, String textAll) {
        return textAll.startsWith(text) ? textAll.substring(text.length()) : null;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (this.suggestions != null) {
            this.suggestions.render(matrixStack);
        }
        checkTextUpdate();
    }

    public void checkTextUpdate() {
        final String inputFieldText = this.chatScreen.inputField.getText();
        if (!StringUtils.equals(this.inputFieldTextLast, inputFieldText)) {
            this.inputFieldTextLast = inputFieldText;
            updateSuggestionList(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class EmojiSuggestions {

        private final Rectangle2d area;
        private final Suggestions suggestions;
        private String currentText;
        private int index;

        public EmojiSuggestions(int x, int y, int areaWidth, Suggestions suggestions) {
            this.suggestions = suggestions;
            int height = Math.min(suggestions.getList().size(), 10) * (ClientProxy.oldFontRenderer.FONT_HEIGHT + 3);
            this.area = new Rectangle2d(x - 1, y - 3 - height, areaWidth, height);
            this.currentText = EmojiSuggestionHelper.this.chatScreen.inputField.getText();
            setIndex(0);
        }

        public void render(MatrixStack stack){
            for (int i = 0; i < Math.min(this.suggestions.getList().size(), 10); ++i) {
                int pos = (this.index + i) % this.suggestions.getList().size();
                final Suggestion suggestion = this.suggestions.getList().get(pos);
                AbstractGui.fill(stack, this.area.getX(), this.area.getY() + 12 * i, this.area.getX() + this.area.getWidth() + 15, this.area.getY() + 12 * i + 12, 0xD0000000);
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(stack, suggestion.getText(), this.area.getX() + 1, this.area.getY() + 2 + 12 * i, pos == this.index ? 0xFFFFFF00 : 0xFFAAAAAA);
                ClientProxy.oldFontRenderer.drawStringWithShadow(stack, suggestion.getText(), 12 + this.area.getX() + 1, this.area.getY() + 2 + 12 * i, pos == this.index ? 0xFFFFFF00 : 0xFFAAAAAA);
            }
        }

        public void setIndex(int i){
            this.index = i;
            if (this.index < 0) this.index = this.suggestions.getList().size() -1;
            else if (this.index >= this.suggestions.getList().size()) this.index = 0;
            EmojiSuggestionHelper.this.chatScreen.inputField.setSuggestion(trim(EmojiSuggestionHelper.this.chatScreen.inputField.getText(), suggestions.getList().get(this.index).apply(currentText)));
        }

        public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 265) {
                offsetIndex(-1);
                return true;
            } else if (keyCode == 264) {
                offsetIndex(1);
                return true;
            } else if (keyCode == 258) {
                applySuggestion();
                return true;
            } else if ((keyCode == 257 || keyCode == 335)) {
                applySuggestion();
                return true;
            } else if (keyCode == 256) {
                removeSuggestion();
                return true;
            } else if ((keyCode == 262 || keyCode == 263)) {
                EmojiSuggestionHelper.this.chatScreen.inputField.setSuggestion("");
                removeSuggestion();
                return false;
            } else
                return false;
        }

        public void offsetIndex(final int deltaIndex) {
            setIndex(this.index + deltaIndex);
        }

        public void removeSuggestion() {
            EmojiSuggestionHelper.this.suggestions = null;
        }

        public void applySuggestion() {
            final Suggestion suggestion = this.suggestions.getList().get(this.index);
            EmojiSuggestionHelper.this.updating = true;
            EmojiSuggestionHelper.this.chatScreen.inputField.setText(suggestion.apply(this.currentText));
            final int i = suggestion.getRange().getStart() + suggestion.getText().length();
            EmojiSuggestionHelper.this.chatScreen.inputField.setCursorPosition(i);
            EmojiSuggestionHelper.this.chatScreen.inputField.setSelectionPos(i);
            setIndex(this.index);
            EmojiSuggestionHelper.this.updating = false;
        }
    }
}
