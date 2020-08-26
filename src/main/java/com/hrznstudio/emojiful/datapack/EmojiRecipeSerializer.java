package com.hrznstudio.emojiful.datapack;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EmojiRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EmojiRecipe>{

    public static final EmojiRecipeSerializer EMOJI_RECIPE_SERIALIZER = new EmojiRecipeSerializer();

    public IRecipeType<EmojiRecipe> recipeType;

    public EmojiRecipeSerializer() {
        setRegistryName("emojiful", "emoji_recipe");
        this.recipeType = IRecipeType.register("emojiful:emoji_recipe");
    }

    @Override
    public EmojiRecipe read(ResourceLocation recipeId, JsonObject json) {
        return new EmojiRecipe(recipeId, json.get("category").getAsString(), json.get("name").getAsString(), json.get("url").getAsString());
    }

    @Override
    public EmojiRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        return new EmojiRecipe(recipeId, buffer.readString(), buffer.readString(), buffer.readString());
    }

    @Override
    public void write(PacketBuffer buffer, EmojiRecipe recipe) {
        buffer.writeString(recipe.getCategory());
        buffer.writeString(recipe.getName());
        buffer.writeString(recipe.getUrl());
    }

}
