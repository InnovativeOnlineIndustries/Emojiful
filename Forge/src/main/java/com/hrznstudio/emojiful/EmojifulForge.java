package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(Constants.MOD_ID)
public class EmojifulForge {


    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    public static final RegistryObject<EmojiRecipeSerializer> EMOJI_RECIPE_SERIALIZER = Emojiful.RECIPE_SER.register("emoji_recipe", EmojiRecipeSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Constants.MOD_ID);
    public static final RegistryObject<RecipeType<EmojiRecipe>> EMOJI_RECIPE_TYPE = Emojiful.RECIPE_TYPE.register("emoji_recipe_type",() -> RecipeType.simple(new ResourceLocation(Constants.MOD_ID, "emoji_recipe_type" )));

    public EmojifulForge() {

        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
    }

}