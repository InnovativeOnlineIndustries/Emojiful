package com.hrznstudio.emojiful.mixin.fabric;

import com.hrznstudio.emojiful.EmojifulFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MinecraftEmojifulMixin {

    @Redirect(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.PUTFIELD))
    private void emojifulFabric_setScreenInject(Minecraft minecraft, Screen screen){
        EmojifulFabric.ClientHandler.handleScreenInject(minecraft, screen);
    }
}
