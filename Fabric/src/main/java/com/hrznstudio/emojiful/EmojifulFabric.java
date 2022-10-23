package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.gui.EmojifulBedChatScreen;
import com.hrznstudio.emojiful.gui.EmojifulChatScreen;
import com.hrznstudio.emojiful.platform.FabricConfigHelper;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class EmojifulFabric implements ModInitializer {


    public static final RecipeType<EmojiRecipe> EMOJI_RECIPE_TYPE = RecipeType.register(Constants.MOD_ID + ":emoji_recipe_type");
    public static final RecipeSerializer<EmojiRecipe> EMOJI_RECIPE_SERIALIZER = RecipeSerializer.register(Constants.MOD_ID + ":emoji_recipe", new EmojiRecipeSerializer());

    @Override
    public void onInitialize() {
        MidnightConfig.init(Constants.MOD_ID, FabricConfigHelper.class);
    }


    public static class ClientHandler {
        //This compiles to another class, so we get a classloading barrier
        public static void handleScreenInject(Minecraft minecraft, Screen screen) {
            if (screen != null) {
                if (!(screen instanceof EmojifulChatScreen)){
                    if (screen instanceof InBedChatScreen){
                        minecraft.screen = new EmojifulBedChatScreen();
                        minecraft.screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
                    }

                    else if (screen instanceof ChatScreen chatScreen) {
                        minecraft.screen = new EmojifulChatScreen(chatScreen.initial);
                        minecraft.screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
                    }

                }
                else {
                    minecraft.screen = screen;
                }
            } else {
                minecraft.screen = null;
            }

        }
    }
}
