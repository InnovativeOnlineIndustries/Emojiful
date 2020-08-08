package com.hrznstudio.emojiful;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.gson.JsonElement;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import com.hrznstudio.emojiful.gui.EmojiButton;
import com.hrznstudio.emojiful.gui.ParentButton;
import com.hrznstudio.emojiful.gui.TranslucentButton;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientProxy {

    public static ClientProxy PROXY = new ClientProxy();

    public static void registerClient(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PROXY::setup);
        MinecraftForge.EVENT_BUS.addListener(PROXY::guiInit);
    }

    @OnlyIn(Dist.CLIENT)
    public void setup(final FMLClientSetupEvent event) {
        preInitEmojis();
        initEmojis();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EmojifulConfig.init());
        Emojiful.LOGGER.info("Loaded " + Emojiful.EMOJI_LIST.size() + " emojis");
    }

    @OnlyIn(Dist.CLIENT)
    public void guiInit(GuiScreenEvent.InitGuiEvent event){
        if (event.getGui() instanceof ChatScreen){
            int x = event.getGui().width - 160;
            int y = event.getGui().height - 40;
            int amountPage = 5*5;
            List<Button> child = new ArrayList<>();
            List<Button> allExtraChild = new ArrayList<>();
            Emojiful.EMOJI_MAP.keySet().forEach(s -> {
                List<EmojiButton> extraChild = new ArrayList<>();
                List<Button> arrowButtons = new ArrayList<>();
                Emojiful.EMOJI_MAP.get(s).forEach(emoji -> {
                    if (emoji != null && emoji.strings != null){
                        EmojiButton button = new EmojiButton(x - 82 - 42 - 42*(extraChild.size() % 5), y - 22 *((extraChild.size()%amountPage )/ 5), 40, 20, emoji.strings.get(0), p_onPress_2_ -> {
                            ((ChatScreen) event.getGui()).inputField.setText(((ChatScreen) event.getGui()).inputField.getText() + " " + emoji.strings.get(0));
                        }, extraChild.size() / amountPage);
                        button.visible = false;
                        event.addWidget(button);
                        extraChild.add(button);
                        allExtraChild.add(button);
                    }
                });
                ParentButton button = new ParentButton(x - 82, y - 22 * child.size(), 80, 20, s.replace(".yml", ""), b -> {
                    allExtraChild.forEach(ec -> ec.visible = false);
                    arrowButtons.forEach(ec-> ec.visible = true);
                    extraChild.forEach(ec -> ec.visible = false);
                    for (EmojiButton emojiButton : extraChild) {
                        if (emojiButton.getPage() == ((ParentButton) b).getPage()){
                            emojiButton.visible = true;
                        }
                    }
                });
                if (extraChild.size() > amountPage){
                    TranslucentButton rightButton = new TranslucentButton(x - 82 - 42, y - 22*6, 40, 20, ">", p_onPress_1_ -> {
                        int page = button.getPage() + 1;
                        if (page > extraChild.size() / amountPage) page = 0;
                        button.setPage(page);
                    });
                    TranslucentButton leftButton = new TranslucentButton(x - 82 - 42 - 42*4, y - 22*6, 40, 20, "<", p_onPress_1_ -> {
                        int page = button.getPage() - 1;
                        if (page < 0) page = extraChild.size() / amountPage;
                        button.setPage(page);
                    });
                    rightButton.visible = false;
                    leftButton.visible = false;
                    arrowButtons.add(rightButton);
                    arrowButtons.add(leftButton);
                    allExtraChild.add(rightButton);
                    allExtraChild.add(leftButton);
                    event.addWidget(rightButton);
                    event.addWidget(leftButton);
                }
                button.visible = false;
                event.addWidget(button);
                child.add(button);
            });
            event.addWidget(new TranslucentButton(x, y, 40, 20, "Emoji", p_onPress_1_ -> {
                child.forEach(button -> button.visible = true);
            }));

        }
    }

    private void preInitEmojis() {
        loadCustomEmojis();
        loadGithubEmojis();
    }

    private void loadCustomEmojis(){
        try {
            YamlReader reader = new YamlReader(new StringReader(Emojiful.readStringFromURL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/emojiful-assets/master/Categories.yml")));
            ArrayList<String> categories = (ArrayList<String>) reader.read();
            for (String category : categories) {
                List<Emoji> emojis = Emojiful.readCategory(category);
                Emojiful.EMOJI_LIST.addAll(emojis);
                Emojiful.EMOJI_MAP.put(category, emojis);
            }
        } catch (YamlException e) {
            Emojiful.error = true;
        }
    }

    private void loadApiEmojis(){
        for (JsonElement categories : Emojiful.readJsonFromUrl("https://www.emojidex.com/api/v1/categories").getAsJsonObject().getAsJsonArray("categories")) {
            Emojiful.EMOJI_MAP.put(categories.getAsJsonObject().get("code").getAsString(), new ArrayList<>());
        }

    }

    private void loadGithubEmojis(){
        Emojiful.EMOJI_MAP.put("Github", new ArrayList<>());
        for (Map.Entry<String, JsonElement> entry : Emojiful.readJsonFromUrl("https://api.github.com/emojis").getAsJsonObject().entrySet()) {
            EmojiFromGithub emoji = new EmojiFromGithub();
            emoji.name = entry.getKey();
            emoji.strings = new ArrayList<>();
            emoji.strings.add(":" + entry.getKey() + ":");
            emoji.location = entry.getKey();
            emoji.url = entry.getValue().getAsString();
            Emojiful.EMOJI_MAP.get("Github").add(emoji);
            Emojiful.EMOJI_LIST.add(emoji);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void initEmojis() {
        if (!Emojiful.error) {
            Minecraft.getInstance().fontRenderer = new EmojiFontRenderer(Minecraft.getInstance(), Minecraft.getInstance().fontRenderer);
            Minecraft.getInstance().getRenderManager().textRenderer = Minecraft.getInstance().fontRenderer;
        }
    }

}
