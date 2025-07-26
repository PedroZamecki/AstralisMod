package org.zamecki.astralis.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class AlienVillagerEntity extends LivingEntity implements PolymerEntity {
    private final Property skin = new Property("textures","ewogICJ0aW1lc3RhbXAiIDogMTczODg1NTg3NTk4NSwKICAicHJvZmlsZUlkIiA6ICJhMzBhNzNkMDBmODE0MmRmOWIzMmM1ZDdmNTlmNDFlYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJkaW5ocGh1cmVhbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNzg3ZTUyOTc1MmVlNTY1NzI5NDYyMGVjYjYwNzc1ZmIyNDJhYTgyOGNkODE5YTQzMzc4OTZmYmEwYjMxNmIxIgogICAgfQogIH0KfQ==",
            "jtePbI4IAJAMwAB0zaSl8xIAcKWn23RMGYWRrgqRwl2QNqd1aTpAIrLjdTj3j6JFukv5zQ8kOqqdGTRe1G5LSH1D5VRLXCyQzlY7lGGZFCicdDVYRLf3/gjXSova4lsa87kahZZXOZa0hEauDTcE+d4nAKXBsBG6HOhjomA+/jH0vFqahCB01Rx4Pgn9RoSGaOPFVsnd7/0CoKF9QTdrKJY2RDWjMSc15tzDlfKnGpLsYIxBPnnkZryD5+LHboyqGnpiQsfzgj+BqfcqLCZC1/Afou9Nr+Mkfk6U6+sb7GHFyEv7hSSGRR/ZB5BRxGXM2KA1lexksIRZbsPFZ+vP7icVu4zA10DxLIwnlCysAcN3ZV7Ejtcqjt7WcFGqzhGC8+ibfHF1KTuSPs0dIpa/joh9aLWCEKui+umxOpJsqhRiiTK/EWrqURUxQ7Yf38lCHVgmPnv21Qu3pv+U27fmsSShiRzkvTsB3DnuGAFk6leWYU50NC+uOyTUkaeeRSriO5pPGXPjMdzLSUNfuz6cjTWAvwqHti3tD4vgt6ukbk0V3doeyXra0YFj/potmNW0nTqlGC5jVHH8reNEZASPE33InC7GRoScySeT8YTgJ8sZGYRdY5pTPKHnJ70ERTIDpYqdvyzV9GFIYXnYDe1grXElotXDVMYPwROIWSgLsZQ="
    );

    protected AlienVillagerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    protected void readCustomData(ReadView view) {
    }

    @Override
    protected void writeCustomData(WriteView view) {
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PLAYER;
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        PolymerEntity.super.onEntityPacketSent(consumer, packet);
        if (packet instanceof EntitySetHeadYawS2CPacket headYawS2CPacket) {
            var ent = (Entity) this;
            consumer.accept(new EntityS2CPacket.Rotate(ent.getId(), MathHelper.packDegrees(headYawS2CPacket.getHeadYaw()), (byte) (ent.getPitch() * 256.0F / 360.0F), ent.isOnGround()));
        }
    }

    public static DefaultAttributeContainer.Builder createAlienVillagerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    public List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> items, ServerPlayerEntity player) {
        items.removeIf(x -> x.getFirst() == EquipmentSlot.HEAD);
        if (PolymerResourcePackUtils.hasMainPack(player)) {
            items.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.HEAD, Items.GLASS.getDefaultStack()));
        }
        return PolymerEntity.super.getPolymerVisibleEquipment(items, player);
    }

    @Override
    public void onBeforeSpawnPacket(ServerPlayerEntity player, Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER));
        var profile = new GameProfile((this).getUuid(), "Alien Villager");
        profile.getProperties().put("textures", this.skin);
        packet.getEntries().add(new PlayerListS2CPacket.Entry(profile.getId(), profile, false, Integer.MAX_VALUE,  GameMode.ADVENTURE, Text.empty(), true,0, null));
        packetConsumer.accept(packet);
    }

    @Override
    public void modifyRawEntityAttributeData(List<EntityAttributesS2CPacket.Entry> data, ServerPlayerEntity player, boolean initial) {
        PolymerEntity.super.modifyRawEntityAttributeData(data, player, initial);

        if (initial) {
            data.add(new EntityAttributesS2CPacket.Entry(
                    EntityAttributes.SCALE,
                    1.1f,
                    List.of()
            ));
        }
    }
}
