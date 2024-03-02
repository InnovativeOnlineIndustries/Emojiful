package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.platform.ForgeConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@Mod(Constants.MOD_ID)
public class EmojifulForge {

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, EmojiRecipeSerializer> EMOJI_RECIPE_SERIALIZER = RECIPE_SER.register("emoji_recipe", EmojiRecipeSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(Registries.RECIPE_TYPE, Constants.MOD_ID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<EmojiRecipe>> EMOJI_RECIPE_TYPE = RECIPE_TYPE.register("emoji_recipe_type", () -> RecipeType.simple(new ResourceLocation(Constants.MOD_ID, "emoji_recipe_type")));

    public EmojifulForge(IEventBus eventBus) {
        RECIPE_SER.register(eventBus);
        RECIPE_TYPE.register(eventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHelper.setup(new ModConfigSpec.Builder()));
        eventBus.addListener(this::handleClientSetup);
    }

    private void handleClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ForgeClientHandler::onRecipesUpdated);
        NeoForge.EVENT_BUS.addListener(ForgeClientHandler::hijackScreen);
    }

}