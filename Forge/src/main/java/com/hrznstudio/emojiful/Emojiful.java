package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import com.hrznstudio.emojiful.platform.ForgeConfigHelper;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("emojiful")
public class Emojiful {
    public static final String MODID = "emojiful";
    public static final Logger LOGGER = LogManager.getLogger("Emojiful");

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<EmojiRecipeSerializer> EMOJI_RECIPE_SERIALIZER = Emojiful.RECIPE_SER.register("emoji_recipe", EmojiRecipeSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final RegistryObject<RecipeType<EmojiRecipe>> EMOJI_RECIPE_TYPE = Emojiful.RECIPE_TYPE.register("emoji_recipe_type",() -> RecipeType.simple(new ResourceLocation(MODID, "emoji_recipe_type" )));
    public Emojiful() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::registerClient);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHelper.setup(new ForgeConfigSpec.Builder()));
        RECIPE_SER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
        //FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RecipeSerializer.class, EventPriority.NORMAL, false, RegistryEvent.Register.class, this::registerSerializable );
    }





}
