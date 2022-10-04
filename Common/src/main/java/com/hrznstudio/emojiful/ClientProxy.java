package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.gson.JsonElement;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import com.hrznstudio.emojiful.api.EmojiFromTwitmoji;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.gui.EmojiSelectionGui;
import com.hrznstudio.emojiful.gui.EmojiSuggestionHelper;
import com.hrznstudio.emojiful.platform.Services;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import com.hrznstudio.emojiful.util.ProfanityFilter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class ClientProxy {

    public static ClientProxy PROXY = new ClientProxy();
    public static Font oldFontRenderer;
    public static List<String> ALL_EMOJIS = new ArrayList<>();
    public static HashMap<EmojiCategory, List<Emoji[]>> SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
    public static List<Emoji> EMOJI_WITH_TEXTS = new ArrayList<>();
    public static final List<EmojiCategory> CATEGORIES = new ArrayList<>();
    public static int lineAmount;

    public static EmojiSuggestionHelper emojiSuggestionHelper;
    public static EmojiSelectionGui emojiSelectionGui;

    public static void setup() {
        preInitEmojis();
        initEmojis();
        indexEmojis();
        Constants.LOG.info("Loaded " + Constants.EMOJI_LIST.size() + " emojis");
    }

    /**
     *
     * @param screen Originally called in Forge's ScreenEvent.Init.Pos events
     */
    public static void guiInit(Screen screen){
        if (screen instanceof ChatScreen && !Constants.error){
            if (Services.CONFIG.showEmojiAutocomplete()) emojiSuggestionHelper = new EmojiSuggestionHelper((ChatScreen) screen);
            if (Services.CONFIG.showEmojiSelector()) emojiSelectionGui = new EmojiSelectionGui((ChatScreen) screen);
        }
    }

    private static void indexEmojis(){
        ALL_EMOJIS = Constants.EMOJI_LIST.stream().map(emoji -> emoji.strings).flatMap(Collection::stream).collect(Collectors.toList());
        SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
        for (EmojiCategory category : CATEGORIES) {
            ++lineAmount;
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : Constants.EMOJI_MAP.getOrDefault(category.name(), new ArrayList<>())) {
                array[i] = emoji;
                ++i;
                if (i >= array.length){
                    SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                    array = new Emoji[9];
                    i = 0;
                    ++lineAmount;
                }
            }
            if (i > 0){
                SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                ++lineAmount;
            }
        }
    }

    /**
     *  Originally called in Forge's ScreenEvent.Render.Post event
     * @param poseStack The posestack to act on
     * @param mouseX Position x of the mouse
     * @param mouseY Position y of the mouse
     */
    public static void render(PoseStack poseStack, double mouseX, double mouseY){
        if (emojiSuggestionHelper != null) emojiSuggestionHelper.render(poseStack);
        if (emojiSelectionGui != null){
            emojiSelectionGui.mouseMoved(mouseX, mouseY);
            emojiSelectionGui.render(poseStack);
        }
    }

    /**
     * Originally called in Forge's KeyPressed Event
     * @param keyCode The key being pressed
     */
    public static boolean onKeyPressed(int keyCode, int scanCode, int modifiers){
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers)) return true;
        return emojiSelectionGui != null && emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers);
    }


    /**
     * Originally called on ScreenEvent.MouseButtonPressed.Pre
     */

    public static void onClick(double mouseX, double mouseY, int button){
        if (emojiSelectionGui != null) emojiSelectionGui.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Originally called on ScreenEvent.MouseScrolled.Pre event
     */
    public static void onScroll(double mouseX, double mouseY, double scrollDelta){
        if (emojiSelectionGui != null) emojiSelectionGui.mouseScrolled(mouseX, mouseY, scrollDelta);
    }


    /**
     * On Screen closed
     */
    public void onClose(){
        if (emojiSelectionGui != null && Minecraft.getInstance().screen != emojiSelectionGui.getChatScreen()) emojiSelectionGui = null;
    }

    /**
     *
     * On ScreenEvent.CharacterTyped event
     * boolean represents cancelability
     */
    public static boolean onCharTyped(char character, int modifiers){
        return (emojiSelectionGui != null && emojiSelectionGui.charTyped(character, modifiers));
    }

    
    public void onRecipesUpdated(){
        CATEGORIES.removeIf(EmojiCategory::worldBased);
        Constants.EMOJI_LIST.removeIf(emoji -> emoji.worldBased);
        Constants.EMOJI_MAP.values().forEach(emojis -> emojis.removeIf(emoji -> emoji.worldBased));
        if (Services.CONFIG.loadDatapack()){
            for (EmojiRecipe emojiRecipe : /*event.getRecipeManager().getAllRecipesFor(Emojiful.EMOJI_RECIPE_TYPE.get()) */ new EmojiRecipe[]{}) {
                EmojiFromGithub emoji = new EmojiFromGithub();
                emoji.name = emojiRecipe.getName();
                emoji.strings = new ArrayList<>();
                emoji.strings.add(":" + emojiRecipe.getName() + ":");
                emoji.location = emojiRecipe.getName();
                emoji.url = emojiRecipe.getUrl();
                emoji.worldBased = true;
                Constants.EMOJI_MAP.computeIfAbsent(emojiRecipe.getCategory(), s -> new ArrayList<>()).add(emoji);
                Constants.EMOJI_LIST.add(emoji);
                if (CATEGORIES.stream().noneMatch(emojiCategory -> emojiCategory.name().equalsIgnoreCase(emojiRecipe.getCategory()))){
                    CATEGORIES.add(0, new EmojiCategory(emojiRecipe.getCategory(), true));
                }
            }
            indexEmojis();
        }
    }

    private static void preInitEmojis() {
        CATEGORIES.addAll(Arrays.asList("Smileys & Emotion", "Animals & Nature", "Food & Drink", "Activities", "Travel & Places", "Objects", "Symbols", "Flags").stream().map(s -> new EmojiCategory(s, false)).collect(Collectors.toList()));
        if (Services.CONFIG.loadCustom())loadCustomEmojis();
        //loadGithubEmojis();
        if (Services.CONFIG.loadTwemoji())loadTwemojis();
        if (Services.CONFIG.getProfanityFilter()) ProfanityFilter.loadConfigs();
    }

    private static void loadCustomEmojis(){
        try {
            YamlReader reader = new YamlReader(new StringReader(CommonClass.readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/master/Categories.yml")));
            ArrayList<String> categories = (ArrayList<String>) reader.read();
            for (String category : categories) {
                CATEGORIES.add(0, new EmojiCategory(category.replace(".yml", ""), false));
                List<Emoji> emojis = CommonClass.readCategory(category);
                Constants.EMOJI_LIST.addAll(emojis);
                Constants.EMOJI_MAP.put(category.replace(".yml", ""), emojis);
            }
        } catch (Exception e) {
            Constants.error = true;
            Constants.LOG.error("An exception was caught whilst loading custom emojis", e);
        }
    }

    private static void loadApiEmojis(){
        for (JsonElement categories : CommonClass.readJsonFromUrl("https://www.emojidex.com/api/v1/categories").getAsJsonObject().getAsJsonArray("categories")) {
            Constants.EMOJI_MAP.put(categories.getAsJsonObject().get("code").getAsString(), new ArrayList<>());
        }
    }

    public static void loadGithubEmojis(){
        Constants.EMOJI_MAP.put("Github", new ArrayList<>());
        for (Map.Entry<String, JsonElement> entry : CommonClass.readJsonFromUrl("https://api.github.com/emojis").getAsJsonObject().entrySet()) {
            EmojiFromGithub emoji = new EmojiFromGithub();
            emoji.name = entry.getKey();
            emoji.strings = new ArrayList<>();
            emoji.strings.add(":" + entry.getKey() + ":");
            emoji.location = entry.getKey();
            emoji.url = entry.getValue().getAsString();
            Constants.EMOJI_MAP.get("Github").add(emoji);
            Constants.EMOJI_LIST.add(emoji);
        }
    }

    public static void loadTwemojis(){
        try{
            for (JsonElement element : CommonClass.readJsonFromUrl("https://raw.githubusercontent.com/iamcal/emoji-data/master/emoji.json").getAsJsonArray()){
                if (element.getAsJsonObject().get("has_img_twitter").getAsBoolean()){
                    EmojiFromTwitmoji emoji = new EmojiFromTwitmoji();
                    emoji.name = element.getAsJsonObject().get("short_name").getAsString();
                    emoji.location = element.getAsJsonObject().get("image").getAsString();
                    emoji.sort =  element.getAsJsonObject().get("sort_order").getAsInt();
                    element.getAsJsonObject().get("short_names").getAsJsonArray().forEach(jsonElement -> emoji.strings.add(":" + jsonElement.getAsString() + ":"));
                    if (emoji.strings.contains(":face_with_symbols_on_mouth:")){
                        emoji.strings.add(":swear:");
                    }
                    if (!element.getAsJsonObject().get("texts").isJsonNull()){
                        element.getAsJsonObject().get("texts").getAsJsonArray().forEach(jsonElement -> emoji.texts.add(jsonElement.getAsString()));
                    }
                    Constants.EMOJI_MAP.computeIfAbsent(element.getAsJsonObject().get("category").getAsString(), s -> new ArrayList<>()).add(emoji);
                    Constants.EMOJI_LIST.add(emoji);
                    if (emoji.texts.size() > 0){
                        ClientProxy.EMOJI_WITH_TEXTS.add(emoji);
                    }
                }
            }
            ClientProxy.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(o -> o.sort));
            Constants.EMOJI_MAP.values().forEach(emojis -> emojis.sort(Comparator.comparingInt(o -> o.sort)));
        } catch (Exception e){
            Constants.error = true;
            Constants.LOG.error("Emojiful found an error while loading",e);
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

