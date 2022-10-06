package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.ClientEmojiHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftEmojifulMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At(value = "TAIL"))
    private void emojifulFabric_initEmojis(CallbackInfo cinfo){
        ClientEmojiHandler.setup();
    }
}
