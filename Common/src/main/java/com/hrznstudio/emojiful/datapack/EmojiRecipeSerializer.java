package com.hrznstudio.emojiful.datapack;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EmojiRecipeSerializer implements RecipeSerializer<EmojiRecipe>{


    public EmojiRecipeSerializer() {

    }

    @Override
    public EmojiRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        return new EmojiRecipe(recipeId, json.get("category").getAsString(), json.get("name").getAsString(), json.get("url").getAsString());
    }

    @Override
    public EmojiRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return new EmojiRecipe(recipeId, buffer.readUtf(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EmojiRecipe recipe) {
        buffer.writeUtf(recipe.getCategory());
        buffer.writeUtf(recipe.getName());
        buffer.writeUtf(recipe.getUrl());
    }

}
