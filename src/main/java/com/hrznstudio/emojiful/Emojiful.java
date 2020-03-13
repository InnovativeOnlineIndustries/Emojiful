package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.collect.Lists;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod(modid = Emojiful.MODID, name = "Emojiful", version = Emojiful.VERSION, clientSideOnly = true)
public class Emojiful {
    public static final String MODID = "emojiful";
    public static final String VERSION = "1.0.3";

    public static final Minecraft MC = Minecraft.getMinecraft();

    public static final Map<String, List<Emoji>> EMOJI_MAP = new HashMap<>();
    public static final List<Emoji> EMOJI_LIST = new ArrayList<>();
    boolean error = false;

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

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
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

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        if (!error)
            MC.fontRenderer = new EmojiFontRenderer(MC);
    }
}