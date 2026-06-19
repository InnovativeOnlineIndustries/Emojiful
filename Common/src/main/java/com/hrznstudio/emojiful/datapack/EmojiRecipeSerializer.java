package com.hrznstudio.emojiful.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class EmojiRecipeSerializer {
    public static final MapCodec<EmojiRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("category").forGetter(EmojiRecipe::getCategory),
                    Codec.STRING.fieldOf("name").forGetter(EmojiRecipe::getName),
                    Codec.STRING.fieldOf("url").forGetter(EmojiRecipe::getUrl)
            ).apply(instance, EmojiRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EmojiRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, recipe) -> {
                        buffer.writeUtf(recipe.getCategory());
                        buffer.writeUtf(recipe.getName());
                        buffer.writeUtf(recipe.getUrl());
                    },
                    buffer -> new EmojiRecipe(buffer.readUtf(), buffer.readUtf(), buffer.readUtf())
            );

    public static final RecipeSerializer<EmojiRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private EmojiRecipeSerializer() {
    }
}
