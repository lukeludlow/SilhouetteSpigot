package dev.lukel.silhouetteserver;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;


public class PacketBuilder {

    public IPacketContainer buildPlayerSpawnPacket(Player player) {
        getLogger().info("buildPlayerSpawnPacket");
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
                .write(0, player.isOnGround());  // FIXME this could cause issues bc deprecated but idk
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

    private byte toAngle(float f) {
        return (byte)((int)(f * 256.0F / 360.0F));
    }

    private short calculateDelta(double previous, double current) {
        // Change in X position as (currentX * 32 - prevX * 32) * 128    (source: https://wiki.vg/Protocol#Entity_Position)
        return (short) ((current * 32 - previous * 32) * 128);
    }

}
