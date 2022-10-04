package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromGithub;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraftforge.client.event.RecipesUpdatedEvent;

import java.util.ArrayList;

public class ForgeClientHandler {

    public static void onRecipesUpdated(RecipesUpdatedEvent event){
        ClientProxy.CATEGORIES.removeIf(EmojiCategory::worldBased);
        Constants.EMOJI_LIST.removeIf(emoji -> emoji.worldBased);
        Constants.EMOJI_MAP.values().forEach(emojis -> emojis.removeIf(emoji -> emoji.worldBased));
        if (Services.CONFIG.loadDatapack()){
            for (EmojiRecipe emojiRecipe : event.getRecipeManager().getAllRecipesFor(Emojiful.EMOJI_RECIPE_TYPE.get())) {
                EmojiFromGithub emoji = new EmojiFromGithub();
                emoji.name = emojiRecipe.getName();
                emoji.strings = new ArrayList<>();
                emoji.strings.add(":" + emojiRecipe.getName() + ":");
                emoji.location = emojiRecipe.getName();
                emoji.url = emojiRecipe.getUrl();
                emoji.worldBased = true;
                Constants.EMOJI_MAP.computeIfAbsent(emojiRecipe.getCategory(), s -> new ArrayList<>()).add(emoji);
                Constants.EMOJI_LIST.add(emoji);
                if (ClientProxy.CATEGORIES.stream().noneMatch(emojiCategory -> emojiCategory.name().equalsIgnoreCase(emojiRecipe.getCategory()))){
                    ClientProxy.CATEGORIES.add(0, new EmojiCategory(emojiRecipe.getCategory(), true));
                }
            }
            ClientProxy.indexEmojis();
        }
    }
}
