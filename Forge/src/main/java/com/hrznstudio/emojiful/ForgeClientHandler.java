package com.hrznstudio.emojiful;

import net.minecraftforge.client.event.RecipesUpdatedEvent;


public class ForgeClientHandler {

    public static void onRecipesUpdated(final RecipesUpdatedEvent event){
        CommonClass.onRecipesUpdated(event.getRecipeManager());
    }
}
