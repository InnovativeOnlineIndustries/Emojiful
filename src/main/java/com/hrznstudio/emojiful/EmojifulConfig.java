package com.hrznstudio.emojiful;

import net.minecraftforge.common.config.Config;

@Config(modid = Emojiful.MODID,name = "Emojiful")
public class EmojifulConfig {

    @Config.Name("emoji_render")
    @Config.LangKey("config.emojiful.emoji_render")
    public static boolean renderEmoji = true;

}
