package com.hrznstudio.emojiful.mixin.fabric;

import com.hrznstudio.emojiful.CommonClass;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow
    abstract RecipeManager getRecipeManager();

    @Inject(method = "handleUpdateRecipes(Lnet/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket;)V", at = @At(value = "TAIL"))
    public void emojiful_recipesUpdated(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket, CallbackInfo callbackInfo) {
        CommonClass.onRecipesUpdated(getRecipeManager());
    }
}
