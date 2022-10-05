package com.hrznstudio.emojiful.mixin.fabric;

import com.hrznstudio.emojiful.ClientEmojiHandler;
import com.hrznstudio.emojiful.EmojifulFabric;
import com.hrznstudio.emojiful.gui.EmojifulChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftEmojifulMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At(value = "TAIL"))
    private void emojifulFabric_initEmojis(CallbackInfo cinfo){
        ClientEmojiHandler.setup();
    }


    @Redirect(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.PUTFIELD))
    private void emojifulFabric_setScreenInject(Minecraft minecraft, Screen screen){
        EmojifulFabric.handleScreenInject(minecraft, screen);
    }
}
