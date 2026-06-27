package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.render.EmojiFontHelper;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ComponentRenderUtils.class)
public class ComponentRenderUtilsMixin {
    @ModifyVariable(
            method = "wrapComponents",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static FormattedText emojiful$prepareChatForWrapping(FormattedText text) {
        return EmojiFontHelper.prepareForWrapping(text);
    }
}
