package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.datapack.EmojiRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
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

    public static final Map<String, List<Emoji>> EMOJI_MAP = new HashMap<>();
    public static final List<Emoji> EMOJI_LIST = new ArrayList<>();
    public static boolean error = false;

    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<EmojiRecipeSerializer> EMOJI_RECIPE_SERIALIZER = Emojiful.RECIPE_SER.register("emoji_recipe", EmojiRecipeSerializer::new);

    public static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final RegistryObject<RecipeType<EmojiRecipe>> EMOJI_RECIPE_TYPE = Emojiful.RECIPE_TYPE.register("emoji_recipe_type",() -> RecipeType.simple(new ResourceLocation(MODID, "emoji_recipe_type" )));
    public Emojiful() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::registerClient);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EmojifulConfig.init());
        RECIPE_SER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
        //FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RecipeSerializer.class, EventPriority.NORMAL, false, RegistryEvent.Register.class, this::registerSerializable );
    }

    public static void main(String[] s) throws YamlException {
        ClientProxy.PROXY.loadTwemojis();
        //{"code":"at","moji":"🇦🇹","unicode":"1f1e6-1f1f9","category":"symbols","tags":[],"link":null,"base":"at","variants":["at"],"score":0,"r18":false,"customizations":[],"combinations":[]}
    }

    public static List<Emoji> readCategory(String cat) throws YamlException {
        YamlReader categoryReader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/master/" + cat)));
        return Lists.newArrayList(categoryReader.read(Emoji[].class));
    }

    public static String readStringFromURL(String requestURL) {
        try {
            try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JsonElement readJsonFromUrl(String url) {
        String jsonText = readStringFromURL(url);
        JsonElement json = new JsonParser().parse(jsonText);
        return json;
    }

}
