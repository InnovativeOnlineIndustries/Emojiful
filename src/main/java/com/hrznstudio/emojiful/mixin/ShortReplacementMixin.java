package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.ClientProxy;
import com.hrznstudio.emojiful.EmojifulConfig;
import com.hrznstudio.emojiful.api.Emoji;
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
        if (EmojifulConfig.getInstance().renderEmoji.get()
                && EmojifulConfig.getInstance().shortEmojiReplacement.get()) {
            for (Emoji emoji : ClientProxy.EMOJI_WITH_TEXTS) {
                if (emoji.texts.size() > 0) {
                    message = message.replaceAll(emoji.getTextRegex(), emoji.getShorterString());
                }
            }
        }
        return message;
    }
}
