package org.zamecki.astralis.entity;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.importer.BbModelImporter;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static org.zamecki.astralis.Astralis.MOD_ID;

public class SkulkAnt extends AnimalEntity implements AnimatedEntity {
    public static final Model MODEL = AjBlueprintLoader.load(Identifier.of(MOD_ID, "skulk_ant"));
    private final EntityHolder<?> holder;

    protected SkulkAnt(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);

        // Creates model holder without of the box support for most LivingEntity features.
        // Note that it is always possible to write your own or override some of their methods.
        this.holder = new LivingEntityHolder<>(this, MODEL);

        // Attaches the holder to this entity in Polymer.
        // Make sure that ticking is enabled, as it is required for model updates.
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected void writeCustomData(WriteView view) {

    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
