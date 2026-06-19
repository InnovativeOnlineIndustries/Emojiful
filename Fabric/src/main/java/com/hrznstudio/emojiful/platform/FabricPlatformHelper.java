package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.EmojifulFabric;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public RecipeSerializer<EmojiRecipe> getRecipeSerializer() {
        return EmojifulFabric.EMOJI_RECIPE_SERIALIZER;
    }
}
