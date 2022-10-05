package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.platform.FabricConfigHelper;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class EmojifulFabric implements ModInitializer {


    public static final RecipeType<EmojiRecipe> EMOJI_RECIPE_TYPE = RecipeType.register(Constants.MOD_ID + "emoji_recipe_type");
    public static final RecipeSerializer<EmojiRecipe> EMOJI_RECIPE_SERIALIZER = RecipeSerializer.register(Constants.MOD_ID + "emoji_recipe", new EmojiRecipeSerializer());
    @Override
    public void onInitialize() {
        MidnightConfig.init(Constants.MOD_ID, FabricConfigHelper.class);
    }
}
