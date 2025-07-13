package org.zamecki.astralis.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

import static com.mojang.text2speech.Narrator.LOGGER;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Inject(method = "register*", at = @At("HEAD"), cancellable = true)
    private static void onRegister(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
        if (key.getValue().toString().endsWith("reinforced_deepslate")) {
            LOGGER.info("Attempting to register reinforced_deepslate block with settings: {}", settings);
            AbstractBlock.Settings reinforcedSettings = AbstractBlock.Settings.create()
                    .mapColor(MapColor.DEEPSLATE_GRAY)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .sounds(BlockSoundGroup.DEEPSLATE)
                    .strength(-1.0F, 3600000.0F);
            Block block = factory.apply(reinforcedSettings.registryKey(key));
            cir.setReturnValue(Registry.register(Registries.BLOCK, key, block));
        }
        // Let vanilla handle other blocks
    }
}
