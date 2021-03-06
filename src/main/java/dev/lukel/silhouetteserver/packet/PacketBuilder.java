package dev.lukel.silhouetteserver.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Optional;


public class PacketBuilder {

    public IPacketContainer buildPlayerSpawnPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packet.getIntegers()
                .write(0, player.getEntityId());
        packet.getUUIDs()
                .write(0, player.getUniqueId());
        packet.getDoubles()
                .write(0, player.getLocation().getX())
                .write(1, player.getLocation().getY())
                .write(2, player.getLocation().getZ());
        packet.getBytes()
                .write(0, toAngle(player.getLocation().getYaw()))
                .write(1, toAngle(player.getLocation().getPitch()));
        return new IPacketContainer(packet);
    }

    public IPacketContainer buildMoveLookPacket(Player player, Location previousLocation, Location currentLocation) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
        packet.getIntegers()
                .write(0, player.getEntityId());
        packet.getShorts()
                .write(0, calculateDelta(previousLocation.getX(), currentLocation.getX()))
                .write(1, calculateDelta(previousLocation.getY(), currentLocation.getY()))
                .write(2, calculateDelta(previousLocation.getZ(), currentLocation.getZ()));
        packet.getBytes()
                .write(0, toAngle(player.getLocation().getYaw()))
                .write(1, toAngle(player.getLocation().getPitch()));
        packet.getBooleans()
                .write(0, player.isOnGround());
        return new IPacketContainer(packet);
    }

    // "While sending the Entity Look packet changes the vertical rotation of the head, sending this packet appears to be necessary to rotate the head horizontally."
    public IPacketContainer buildHeadLookPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers()
                .write(0, player.getEntityId());
        packet.getBytes()
                .write(0, toAngle(player.getLocation().getYaw()));
        return new IPacketContainer(packet);
    }

    public IPacketContainer buildDestroyEntityPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists()
                .write(0, List.of(player.getEntityId()));
        return new IPacketContainer(packet);
    }

    public IPacketContainer buildEntityMetadataPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers()
                .write(0, player.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setEntity(player);
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(EntityPose.class);
        watcher.setObject(6, serializer, bukkitPoseToNmsPose(player.getPose()));
        if (player.getPose() == Pose.SLEEPING) {
            Location l = player.getLocation();
            BlockPosition block = new BlockPosition(l.getX(), l.getY(), l.getZ());
            // set block position with optional=true
            watcher.setObject(14, WrappedDataWatcher.Registry.get(BlockPosition.class, true), Optional.of(block));
        }
        packet.getWatchableCollectionModifier()
                .write(0, watcher.getWatchableObjects());
        return new IPacketContainer(packet);
    }

    public IPacketContainer buildEntityZeroHealthPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers()
                .write(0, player.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setEntity(player);
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Float.class);
        watcher.setObject(9, serializer, 0.0f);
        packet.getWatchableCollectionModifier()
                .write(0, watcher.getWatchableObjects());
        return new IPacketContainer(packet);
    }

    public List<IPacketContainer> buildPlayerEquipmentPackets(Player player) {
        return List.of(
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.MAINHAND),
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.OFFHAND),
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.FEET),
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.LEGS),
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.CHEST),
                buildPlayerEquipmentPacket(player, EnumWrappers.ItemSlot.HEAD));
    }

    public IPacketContainer buildPlayerEquipmentPacket(Player player, EnumWrappers.ItemSlot itemSlot) {
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment();
        packet.setEntityID(player.getEntityId());
        packet.setSlotStackPair(itemSlot, player.getEquipment().getItem(itemSlotToEquipmentSlot(itemSlot)));
        return new IPacketContainer(packet.getHandle());
    }

    public IPacketContainer buildPlayerAnimationPacket(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
        packet.getIntegers()
                .write(0, player.getEntityId())
                .write(1, 0);  // swing main arm
        return new IPacketContainer(packet);
    }

    public IPacketContainer buildBoatPassengersPacket(Entity boat) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers()
                .write(0, boat.getEntityId());
        int[] passengerIds = boat.getPassengers().stream()
                .mapToInt(Entity::getEntityId)
                .toArray();
        packet.getIntegerArrays()
                .write(0, passengerIds);
        return new IPacketContainer(packet);
    }

    private EntityPose bukkitPoseToNmsPose(Pose pose) {
        return switch (pose) {
            case STANDING -> EntityPose.a;
            case FALL_FLYING -> EntityPose.b;
            case SLEEPING -> EntityPose.c;
            case SWIMMING -> EntityPose.d;
            case SPIN_ATTACK -> EntityPose.e;
            case SNEAKING -> EntityPose.f;
            case LONG_JUMPING -> EntityPose.g;
            case DYING -> EntityPose.h;
            case CROAKING -> null;
            case USING_TONGUE -> null;
            case ROARING -> null;
            case SNIFFING -> null;
            case EMERGING -> null;
            case DIGGING -> null;
        };
    }

    private EquipmentSlot itemSlotToEquipmentSlot(EnumWrappers.ItemSlot itemSlot) {
        return switch (itemSlot) {
            case MAINHAND -> EquipmentSlot.HAND;
            case OFFHAND -> EquipmentSlot.OFF_HAND;
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
        };
    }

    private byte toAngle(float f) {
        return (byte)((int)(f * 256.0F / 360.0F));
    }

    private short calculateDelta(double previous, double current) {
        // (source: https://wiki.vg/Protocol#Entity_Position)
        // Change in X position as (currentX * 32 - prevX * 32) * 128
        return (short) ((current * 32 - previous * 32) * 128);
    }

}
