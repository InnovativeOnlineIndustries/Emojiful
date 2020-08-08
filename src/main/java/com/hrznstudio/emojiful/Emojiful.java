package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiFromEmojipedia;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import com.hrznstudio.emojiful.gui.EmojiButton;
import com.hrznstudio.emojiful.gui.ParentButton;
import com.hrznstudio.emojiful.gui.TranslucentButton;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.JSONException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("emojiful")
public class Emojiful {
    public static final String MODID = "emojiful";
    public static final Logger LOGGER = LogManager.getLogger("Emojiful");

    public static final Map<String, List<Emoji>> EMOJI_MAP = new HashMap<>();
    public static final List<Emoji> EMOJI_LIST = new ArrayList<>();
    public static boolean error = false;

    public Emojiful() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::registerClient);
    }

    public static void main(String[] s) throws YamlException {
        //YamlReader reader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/master/Categories.yml")));
        //ArrayList<String> categories = (ArrayList<String>) reader.read();
        //for (String category : categories) {
        //    List<Emoji> emojis = readCategory(category);
        //    EMOJI_LIST.addAll(emojis);
        //    EMOJI_MAP.put(category, emojis);
        //}
        for (JsonElement categories : readJsonFromUrl("https://www.emojidex.com/api/v1/categories").getAsJsonObject().getAsJsonArray("categories")) {
            EMOJI_MAP.put(categories.getAsJsonObject().get("code").getAsString(), new ArrayList<>());
        }
        for (JsonElement jsonElement : readJsonFromUrl("https://cdn.emojidex.com/static/utf_emoji.json").getAsJsonArray()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            EmojiFromEmojipedia emoji = new EmojiFromEmojipedia();
            emoji.name = obj.get("code").getAsString();
            emoji.strings = Arrays.asList(emoji.name);
            emoji.location = emoji.name;
            EMOJI_MAP.get(obj.get("category").getAsString()).add(emoji);
            EMOJI_LIST.add(emoji);
        }
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
