package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CommonClass {


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

    public static void main(String[] s) throws YamlException {
        ClientEmojiHandler.loadTwemojis();
    }

    public static List<Emoji> readCategory(String cat) throws YamlException {
        YamlReader categoryReader = new YamlReader(new StringReader(readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/master/" + cat)));
        return Lists.newArrayList(categoryReader.read(Emoji[].class));
    }

    public static void onRecipesUpdated(RecipeManager manager) {
        ClientEmojiHandler.CATEGORIES.removeIf(EmojiCategory::worldBased);
        Constants.EMOJI_LIST.removeIf(Emoji::worldBased);
        if (Services.CONFIG.loadDatapack()) {
            RecipeType<EmojiRecipe> emojiRecipeRecipeType = Services.PLATFORM.getRecipeType();
            List<EmojiRecipe> emojiList = manager.getAllRecipesFor(emojiRecipeRecipeType);
            for (EmojiRecipe emojiRecipe : emojiList) {
                EmojiFromGithub emoji = new EmojiFromGithub();
                emoji.name = emojiRecipe.getName();
                emoji.strings = new ArrayList<>();
                emoji.strings.add(":" + emojiRecipe.getName() + ":");
                emoji.location = emojiRecipe.getName();
                emoji.url = emojiRecipe.getUrl();
                emoji.worldBased = true;
                Constants.EMOJI_MAP.computeIfAbsent(emojiRecipe.getCategory(), s -> new ArrayList<>()).add(emoji);
                Constants.EMOJI_LIST.add(emoji);
                if (ClientEmojiHandler.CATEGORIES.stream().noneMatch(emojiCategory -> emojiCategory.name().equalsIgnoreCase(emojiRecipe.getCategory().toLowerCase()))) {
                    ClientEmojiHandler.CATEGORIES.add(0, new EmojiCategory(emojiRecipe.getCategory(), true));
                }
            }
            ClientEmojiHandler.indexEmojis();
        }
    }

}