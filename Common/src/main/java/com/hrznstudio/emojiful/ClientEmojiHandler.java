package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.gson.JsonElement;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromTwitmoji;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import com.hrznstudio.emojiful.util.ProfanityFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class ClientEmojiHandler {
    public static final List<EmojiCategory> CATEGORIES = new ArrayList<>();
    public static Font oldFontRenderer;
    public static List<String> ALL_EMOJIS = new ArrayList<>();
    public static HashMap<EmojiCategory, List<Emoji[]>> SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
    public static List<Emoji> EMOJI_WITH_TEXTS = new ArrayList<>();
    public static int lineAmount;

    public static void setup() {
        preInitEmojis();
        initEmojis();
        indexEmojis();
        Constants.LOG.info("Loaded " + Constants.EMOJI_LIST.size() + " emojis");
    }

    public static void indexEmojis() {
        ALL_EMOJIS = Constants.EMOJI_LIST.stream().map(emoji -> emoji.strings).flatMap(Collection::stream).collect(Collectors.toList());
        SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
        for (EmojiCategory category : CATEGORIES) {
            ++lineAmount;
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : Constants.EMOJI_MAP.getOrDefault(category.name(), new ArrayList<>())) {
                array[i] = emoji;
                ++i;
                if (i >= array.length) {
                    SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                    array = new Emoji[9];
                    i = 0;
                    ++lineAmount;
                }
            }
            if (i > 0) {
                SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                ++lineAmount;
            }
        }
    }

    private static void preInitEmojis() {
        if (Services.CONFIG.loadCustom()) loadCustomEmojis();
        //loadGithubEmojis();
        if (Services.CONFIG.loadTwemoji()){
            CATEGORIES.addAll(Arrays.asList("Smileys & Emotion", "Animals & Nature", "Food & Drink", "Activities", "Travel & Places", "Objects", "Symbols", "Flags").stream().map(s -> new EmojiCategory(s, false)).collect(Collectors.toList()));
            loadTwemojis();
        }
        if (Services.CONFIG.getProfanityFilter()) ProfanityFilter.loadConfigs();
    }

    private static void loadCustomEmojis() {
        try {
            YamlReader reader = new YamlReader(new StringReader(CommonClass.readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/1.20-plus/Categories.yml")));
            ArrayList<String> categories = (ArrayList<String>) reader.read();
            for (String category : categories) {
                CATEGORIES.add(new EmojiCategory(category.replace(".yml", ""), false));
                List<Emoji> emojis = CommonClass.readCategory(category);
                emojis.forEach(emoji -> emoji.location = CommonClass.cleanURL(emoji.location));
                Constants.EMOJI_LIST.addAll(emojis);
                Constants.EMOJI_MAP.put(category.replace(".yml", ""), emojis);
            }
        } catch (Exception e) {
            Constants.error = true;
            Constants.LOG.error("An exception was caught whilst loading custom emojis", e);
        }
    }

    public static void loadTwemojis() {
        try {
            for (JsonElement element : CommonClass.readJsonFromUrl("https://raw.githubusercontent.com/iamcal/emoji-data/master/emoji.json").getAsJsonArray()) {
                if (element.getAsJsonObject().get("has_img_twitter").getAsBoolean()) {
                    EmojiFromTwitmoji emoji = new EmojiFromTwitmoji();
                    emoji.name = element.getAsJsonObject().get("short_name").getAsString();
                    emoji.location = element.getAsJsonObject().get("image").getAsString();
                    emoji.sort = element.getAsJsonObject().get("sort_order").getAsInt();
                    element.getAsJsonObject().get("short_names").getAsJsonArray().forEach(jsonElement -> emoji.strings.add(":" + jsonElement.getAsString() + ":"));
                    if (emoji.strings.contains(":face_with_symbols_on_mouth:")) {
                        emoji.strings.add(":swear:");
                    }
                    if (!element.getAsJsonObject().get("texts").isJsonNull()) {
                        element.getAsJsonObject().get("texts").getAsJsonArray().forEach(jsonElement -> emoji.texts.add(jsonElement.getAsString()));
                    }
                    Constants.EMOJI_MAP.computeIfAbsent(element.getAsJsonObject().get("category").getAsString(), s -> new ArrayList<>()).add(emoji);
                    Constants.EMOJI_LIST.add(emoji);
                    if (emoji.texts.size() > 0) {
                        ClientEmojiHandler.EMOJI_WITH_TEXTS.add(emoji);
                    }
                }
            }
            ClientEmojiHandler.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(o -> o.sort));
            Constants.EMOJI_MAP.values().forEach(emojis -> emojis.sort(Comparator.comparingInt(o -> o.sort)));
        } catch (Exception e) {
            Constants.error = true;
            Constants.LOG.error("Emojiful found an error while loading", e);
        }
    }

    private static void initEmojis() {
        if (!Constants.error) {
            oldFontRenderer = Minecraft.getInstance().font;
            Minecraft.getInstance().font = new EmojiFontRenderer(Minecraft.getInstance().font);
            Minecraft.getInstance().getEntityRenderDispatcher().font = Minecraft.getInstance().font;
            BlockEntityRenderers.register(BlockEntityType.SIGN, p_173571_ -> {
                SignRenderer signRenderer = new SignRenderer(p_173571_);
                signRenderer.font = Minecraft.getInstance().font;
                return signRenderer;
            });
        }
    }

}

