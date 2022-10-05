package com.hrznstudio.emojiful.mixin.fabric;

import com.hrznstudio.emojiful.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.main.GameConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class FontModificationMixin {
    @Redirect(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fontFilterFishy:Lnet/minecraft/client/gui/Font;", opcode = Opcodes.PUTFIELD))
    private void emojifulFabric_injectedMethod(Minecraft minecraft, Font font){
        minecraft.fontFilterFishy = minecraft.fontManager.createFontFilterFishy();
        ClientProxy.setup();
    }
}
