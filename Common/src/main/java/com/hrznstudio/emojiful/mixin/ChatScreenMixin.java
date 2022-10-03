package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import net.minecraft.client.gui.components.ChatComponent;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public abstract class ChatScreenMixin {

    //empty[style={}, siblings=[
    //     literal{[}[style={color=gold}],
    //     literal{DISCORD}[style={color=dark_purple}],
    //     literal{]}[style={color=gold}],
    //     literal{ <}[style={}],
    //     empty[style={color=#E91E63}, siblings=[
    //        literal{Buuz135}[style={}]]], literal{> }[style={}], empty[style={}, siblings=[literal{test <:infinity_nuke:855924066539536414>}[style={}]]]]]

    @Shadow @Nullable
    public abstract ChatScreen emojiful_getFocusedChat();

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public Component emojiful_component(Component x) {
        emojiful_replaceComponents(x);
        return x;
    }


    private void emojiful_replaceComponents(Component x){
        var components = new ArrayList<Component>();
        for (int i = 0; i < x.getSiblings().size(); i++) {
            emojiful_replaceComponents(x.getSiblings().get(i));
            if (x.getSiblings().get(i).getContents() instanceof LiteralContents literalContents){
                var comp = Component.literal(tryLoadEmoji(literalContents.text())).withStyle(x.getSiblings().get(i).getStyle());
                components.add(comp);
            } else {
                components.add(x.getSiblings().get(i));
            }
        }
        x.getSiblings().clear();
        x.getSiblings().addAll(components);
    }


    private static final Pattern DISCORD_EMOJI = Pattern.compile("<:\\w+:[0-9]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANIMATED_DISCORD_EMOJI = Pattern.compile("<a:\\w+:[0-9]+>", Pattern.CASE_INSENSITIVE);

    private static String tryLoadEmoji(String unformattedText){
        Matcher discordMatcher = DISCORD_EMOJI.matcher(unformattedText);
        while (discordMatcher.find()) {
            var discordValue = discordMatcher.group(0);
            unformattedText = unformattedText.replaceAll(discordValue, loadEmoji(discordValue, false));
        }
        discordMatcher = ANIMATED_DISCORD_EMOJI.matcher(unformattedText);
        while (discordMatcher.find()) {
            var discordValue = discordMatcher.group(0);
            unformattedText = unformattedText.replaceAll(discordValue, loadEmoji(discordValue, true));
        }
        return unformattedText;
    }

    private static String loadEmoji(String discordMatcher, boolean isAnimated){
        var discordEmoji = discordMatcher.replaceAll(isAnimated ? "<a:" : "<:", "").replaceAll(">", "");
        var slug = discordEmoji.split(":")[0];
        var url = discordEmoji.split(":")[1];
        EmojiFromGithub emoji = new EmojiFromGithub();
        emoji.name = slug;
        emoji.strings = new ArrayList<>();
        emoji.strings.add(":" + slug + ":");
        emoji.location = slug;
        emoji.url = "https://cdn.discordapp.com/emojis/" + url + (isAnimated ? ".gif" : ".png");
        emoji.worldBased = true;
        //unformattedText = unformattedText.replaceAll("<:" + discordEmoji + ">",":" + slug + ":");
        Constants.EMOJI_MAP.computeIfAbsent("Discord Relay Emojis", s -> new ArrayList<>()).add(emoji);
        Constants.EMOJI_LIST.add(emoji);
        return ":" + slug + ":";
    }

}
