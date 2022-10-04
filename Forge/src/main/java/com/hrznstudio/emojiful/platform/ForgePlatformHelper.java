package com.hrznstudio.emojiful.platform;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.platform.services.IPlatformHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public RecipeType<EmojiRecipe> getRecipeType() {
        return null;
    }

    @Override
    public RecipeSerializer<EmojiRecipe> getRecipeSerializer() {
        return null;
    }
}
