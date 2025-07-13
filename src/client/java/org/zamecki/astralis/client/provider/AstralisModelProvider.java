package org.zamecki.astralis.client.provider;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;

import static org.zamecki.astralis.block.BlockRegistry.TEST_BLOCK;

public class AstralisModelProvider extends FabricModelProvider {
    public AstralisModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(TEST_BLOCK);
    }


    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(TEST_BLOCK.asItem());
    }

    @Override
    public String getName() {
        return "FabricDocsReference Model Provider";
    }
}
