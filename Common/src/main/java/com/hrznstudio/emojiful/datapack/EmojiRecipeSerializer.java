package com.hrznstudio.emojiful.datapack;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Objects;

public class EmojiRecipeSerializer implements RecipeSerializer<EmojiRecipe> {

    private final Codec<EmojiRecipe> codec;
    public EmojiRecipeSerializer() {
        this.codec = RecordCodecBuilder.create(instance -> {
            var test = instance.group(
                    ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(Recipe::getGroup),
                    Codec.STRING.fieldOf("category").forGetter(EmojiRecipe::getCategory),
                    Codec.STRING.fieldOf("name").forGetter(EmojiRecipe::getName),
                    Codec.STRING.fieldOf("url").forGetter(EmojiRecipe::getUrl));
            return test.apply(instance, (s, s2, s3, s4) -> new EmojiRecipe(s2, s3, s4));
        });
    }


    @Override
    public EmojiRecipe fromNetwork(FriendlyByteBuf buffer) {
        return new EmojiRecipe(buffer.readUtf(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public Codec<EmojiRecipe> codec() {
        return codec;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EmojiRecipe recipe) {
        buffer.writeUtf(recipe.getCategory());
        buffer.writeUtf(recipe.getName());
        buffer.writeUtf(recipe.getUrl());
    }

}
