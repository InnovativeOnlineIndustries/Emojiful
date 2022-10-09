package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.gui.EmojifulChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenEvent;


public class ForgeClientHandler {

    public static void onRecipesUpdated(final RecipesUpdatedEvent event) {
        CommonClass.onRecipesUpdated(event.getRecipeManager());
    }

    public static void hijackScreen(final ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof ChatScreen && !(event.getNewScreen() instanceof EmojifulChatScreen)) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new EmojifulChatScreen());
        }
    }
}
