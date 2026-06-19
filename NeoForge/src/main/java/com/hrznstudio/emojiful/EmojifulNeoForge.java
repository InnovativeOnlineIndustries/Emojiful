package com.hrznstudio.emojiful;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.platform.ForgeConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@Mod(Constants.MOD_ID)
public class EmojifulNeoForge {

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EmojiRecipe>> EMOJI_RECIPE_SERIALIZER =
            RECIPE_SER.register("emoji_recipe", () -> EmojiRecipeSerializer.INSTANCE);

    public EmojifulNeoForge(Dist dist, IEventBus modBus, ModContainer container) {
        RECIPE_SER.register(modBus);
        createAndLoadConfigs(container, ModConfig.Type.STARTUP, ForgeConfigHelper.setup(new ModConfigSpec.Builder()), "emojiful-client.toml");
        modBus.addListener(this::handleClientSetup);
    }

    private void handleClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ForgeClientHandler::onRecipesUpdated);
        NeoForge.EVENT_BUS.addListener(ForgeClientHandler::hijackScreen);
    }

    private static void createAndLoadConfigs(ModContainer modContainer, ModConfig.Type type, ModConfigSpec spec, String path) {
        modContainer.registerConfig(type, spec, path);
    }

}
