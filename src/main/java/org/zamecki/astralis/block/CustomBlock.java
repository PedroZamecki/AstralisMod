package org.zamecki.astralis.block;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class CustomBlock extends Block implements PolymerTexturedBlock {
    private final PolymerBlockModel model;
    private final BlockModelType type;
    private final Block fallbackBlock;

    public CustomBlock(Settings settings, Identifier blockId, BlockModelType type, Block fallbackBlock) {
        super(settings);

        this.model = PolymerBlockModel.of(blockId);
        this.type = type;
        this.fallbackBlock = fallbackBlock;
    }

    public CustomBlock(Settings settings, Identifier blockId, BlockModelType type) {
        super(settings);

        this.model = PolymerBlockModel.of(blockId);
        this.type = type;
        this.fallbackBlock = Blocks.BARRIER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext context) {
        BlockState polymerBlockState = PolymerBlockResourceUtils.requestBlock(this.type, this.model);
        if (polymerBlockState != null) {
            return polymerBlockState;
        }
        return this.fallbackBlock.getDefaultState();
    }
}
