package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.ClientEmojiHandler;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatScreen.class)
public class ShortReplacementMixin {
    @ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
    private String getValueForHandleChatInput(String original) {
        return replaceShortEmojis(original);
    }

    @ModifyVariable(method = "updateChatPreview", at = @At("HEAD"), argsOnly = true)
    private String getValueForUpdateChatPreview(String original) {
        return replaceShortEmojis(original);
    }

    private String replaceShortEmojis(String message) {
        if (Services.CONFIG.renderEmoji() && Services.CONFIG.shortEmojiReplacement()) {
            for (Emoji emoji : ClientEmojiHandler.EMOJI_WITH_TEXTS) {
                if (emoji.texts.size() > 0) {
                    message = message.replaceAll(emoji.getTextRegex(), emoji.getShorterString());
                }
            }
        }
        return message;
    }
}
