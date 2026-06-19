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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EmojifulFabric implements ModInitializer {


    public static final RecipeSerializer<EmojiRecipe> EMOJI_RECIPE_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "emoji_recipe"),
            EmojiRecipeSerializer.INSTANCE
    );

    @Override
    public void onInitialize() {
        MidnightConfig.init(Constants.MOD_ID, FabricConfigHelper.class);
    }


    public static class ClientHandler {
        //This compiles to another class, so we get a classloading barrier
        public static void handleScreenInject(Minecraft minecraft, Screen screen) {
            if (screen != null) {
                if (!(screen instanceof EmojifulChatScreen) && screen instanceof ChatScreen){
                    if (screen instanceof InBedChatScreen){
                        minecraft.screen = new EmojifulBedChatScreen();
                        minecraft.screen.init(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
                    }
                    else  {
                        minecraft.screen = new EmojifulChatScreen(((ChatScreen) screen).initial);
                        minecraft.screen.init(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
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
