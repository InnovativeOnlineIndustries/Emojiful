package com.hrznstudio.emojiful.datapack;

import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class EmojiRecipe extends CustomRecipe {

    private final String category;
    private final String name;
    private final String url;

    public EmojiRecipe(String category, String name, String url) {
        this.category = category;
        this.name = name;
        this.url = url;
    }
    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return Services.PLATFORM.getRecipeSerializer();
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
