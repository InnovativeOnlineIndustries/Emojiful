package com.hrznstudio.emojiful;

import net.fabricmc.api.ClientModInitializer;

public class EmojifulFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientProxy.setup();
    }
}
