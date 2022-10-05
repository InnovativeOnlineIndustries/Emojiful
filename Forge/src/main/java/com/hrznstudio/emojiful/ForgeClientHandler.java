package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.gui.EmojifulChatScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenEvent;


public class ForgeClientHandler {

    public static void onRecipesUpdated(final RecipesUpdatedEvent event){
        CommonClass.onRecipesUpdated(event.getRecipeManager());
    }

    public static void hijackScreen(final ScreenEvent.Opening event){
        if (event.getCurrentScreen() instanceof ChatScreen && !(event.getCurrentScreen() instanceof EmojifulChatScreen)){
            event.setNewScreen(new EmojifulChatScreen());
        }
    }
}
