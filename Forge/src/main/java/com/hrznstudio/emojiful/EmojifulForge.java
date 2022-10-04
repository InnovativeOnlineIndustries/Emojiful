package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.platform.ForgeConfigHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class EmojifulForge {

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    public static final RegistryObject<EmojiRecipeSerializer> EMOJI_RECIPE_SERIALIZER = Emojiful.RECIPE_SER.register("emoji_recipe", EmojiRecipeSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Constants.MOD_ID);
    public static final RegistryObject<RecipeType<EmojiRecipe>> EMOJI_RECIPE_TYPE = Emojiful.RECIPE_TYPE.register("emoji_recipe_type",() -> RecipeType.simple(new ResourceLocation(Constants.MOD_ID, "emoji_recipe_type")));

    public EmojifulForge() {
        RECIPE_SER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHelper.setup(new ForgeConfigSpec.Builder()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleClientSetup);
    }

    private void handleClientSetup(final FMLClientSetupEvent event){
        ClientProxy.setup();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ForgeClientHandler::onRecipesUpdated);
    }

}