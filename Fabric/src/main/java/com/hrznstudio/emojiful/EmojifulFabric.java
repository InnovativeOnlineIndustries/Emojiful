package com.hrznstudio.emojiful;

import net.fabricmc.api.ClientModInitializer;

public class EmojifulFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
    }
}
