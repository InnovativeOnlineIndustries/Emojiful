package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.JSONException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("emojiful")
public class Emojiful {
    public static final String MODID = "emojiful";
    private static final Logger LOGGER = LogManager.getLogger("Emojiful");

    public static final Map<String, List<Emoji>> EMOJI_MAP = new HashMap<>();
    public static final List<Emoji> EMOJI_LIST = new ArrayList<>();
    private boolean error = false;
    private static final Minecraft MC = Minecraft.getInstance();

    public Emojiful() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        preInitEmojis();
        initEmojis();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EmojifulConfig.init());
    }

    private void preInitEmojis() {
        try {
            YamlReader reader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/HrznStudio/Emojiful/master/Categories.yml")));
            ArrayList<String> categories = (ArrayList<String>) reader.read();
            for (String category : categories) {
                List<Emoji> emojis = readCategory(category);
                EMOJI_LIST.addAll(emojis);
                EMOJI_MAP.put(category, emojis);
            }
        } catch (YamlException e) {
            error = true;
        }
    }

    private void initEmojis() {
        if (!error) {
            MC.fontRenderer = new EmojiFontRenderer(MC);
        }
    }

    public static void main(String[] s) throws YamlException {
        YamlReader reader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/HrznStudio/Emojiful/master/Categories.yml")));
        ArrayList<String> categories = (ArrayList<String>) reader.read();
        for (String category : categories) {
            List<Emoji> emojis = readCategory(category);
            EMOJI_LIST.addAll(emojis);
            EMOJI_MAP.put(category, emojis);
        }
    }

    public static List<Emoji> readCategory(String cat) throws YamlException {
        YamlReader categoryReader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/HrznStudio/Emojiful/master/" + cat)));
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



}
