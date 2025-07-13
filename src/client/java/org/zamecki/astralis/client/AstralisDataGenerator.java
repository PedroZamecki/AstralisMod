package org.zamecki.astralis.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.zamecki.astralis.client.provider.AstralisBlockLootTableProvider;
import org.zamecki.astralis.client.provider.AstralisModelProvider;

public class AstralisDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(AstralisBlockLootTableProvider::new);
        pack.addProvider(AstralisModelProvider::new);
    }
}
