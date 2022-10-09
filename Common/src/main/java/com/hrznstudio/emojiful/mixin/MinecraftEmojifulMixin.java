package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.ClientEmojiHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftEmojifulMixin {
    @Inject(method = "<init>*", at = @At(value = "RETURN"))
    private void emojifulFabric_initEmojis(CallbackInfo callbackInfo){
        ClientEmojiHandler.setup();
    }
}
