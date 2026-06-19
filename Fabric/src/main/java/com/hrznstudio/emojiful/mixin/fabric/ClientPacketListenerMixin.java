package com.hrznstudio.emojiful.mixin.fabric;

import com.hrznstudio.emojiful.CommonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleUpdateRecipes(Lnet/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket;)V", at = @At(value = "TAIL"))
    public void emojiful_recipesUpdated(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket, CallbackInfo callbackInfo) {
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            CommonClass.onRecipesUpdated(Minecraft.getInstance().getSingleplayerServer().getRecipeManager());
        }
    }
}
