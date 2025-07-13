package org.zamecki.astralis.client.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

import static org.zamecki.astralis.block.BlockRegistry.TEST_BLOCK;

public class AstralisBlockLootTableProvider extends FabricBlockLootTableProvider {
    public AstralisBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(TEST_BLOCK);
    }
}
