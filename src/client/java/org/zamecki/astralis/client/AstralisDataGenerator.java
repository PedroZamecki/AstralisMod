package org.zamecki.astralis.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AstralisDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        @SuppressWarnings("unused")
        FabricDataGenerator.Pack _pack = fabricDataGenerator.createPack();
    }
}
