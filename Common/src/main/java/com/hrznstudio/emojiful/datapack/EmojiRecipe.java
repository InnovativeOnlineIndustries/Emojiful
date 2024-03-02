package com.hrznstudio.emojiful.datapack;

import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class EmojiRecipe implements Recipe<Container> {

    private final String category;
    private final String name;
    private final String url;

    public EmojiRecipe(String category, String name, String url) {
        this.category = category;
        this.name = name;
        this.url = url;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.PLATFORM.getRecipeSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return Services.PLATFORM.getRecipeType();
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
