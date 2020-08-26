package com.hrznstudio.emojiful.datapack;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EmojiRecipe implements IRecipe<IInventory> {

    private final ResourceLocation recipeName;
    private String category;
    private String name;
    private String url;

    public EmojiRecipe(ResourceLocation recipeName) {
        this.recipeName = recipeName;
    }

    public EmojiRecipe(ResourceLocation recipeName, String category, String name, String url) {
        this.recipeName = recipeName;
        this.category = category;
        this.name = name;
        this.url = url;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeName;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return EmojiRecipeSerializer.EMOJI_RECIPE_SERIALIZER;
    }

    @Override
    public IRecipeType<?> getType() {
        return EmojiRecipeSerializer.EMOJI_RECIPE_SERIALIZER.recipeType;
    }

    public ResourceLocation getRecipeName() {
        return recipeName;
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
