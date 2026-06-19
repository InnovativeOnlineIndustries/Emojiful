package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.gui.EmojifulBedChatScreen;
import com.hrznstudio.emojiful.gui.EmojifulChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;


public class ForgeClientHandler {

    public static void onRecipesUpdated(final RecipesReceivedEvent event) {
        if (!event.getRecipeMap().values().isEmpty()) {
            CommonClass.onRecipesUpdated(event.getRecipeMap().values());
        } else if (Minecraft.getInstance().getSingleplayerServer() != null) {
            CommonClass.onRecipesUpdated(Minecraft.getInstance().getSingleplayerServer().getRecipeManager());
        }
    }

    public static void hijackScreen(final ScreenEvent.Opening event) {
        final Screen newScreen = event.getNewScreen();
        if (newScreen instanceof EmojifulChatScreen || newScreen instanceof EmojifulBedChatScreen){
            return;
        }
        if (event.getNewScreen() instanceof InBedChatScreen){
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new EmojifulBedChatScreen());
        }
        else if (event.getNewScreen() instanceof ChatScreen chatScreen) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new EmojifulChatScreen(chatScreen.initial));
        }
    }
}
