package com.hrznstudio.emojiful;

import net.fabricmc.api.ClientModInitializer;

public class EmojifulFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientProxy.setup();
    }
}
