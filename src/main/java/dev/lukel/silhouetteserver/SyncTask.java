package dev.lukel.silhouetteserver;

import dev.lukel.silhouetteserver.player.BukkitCraftPlayer;
import dev.lukel.silhouetteserver.player.BukkitCraftPlayerFactory;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.HashMap;

import static org.bukkit.Bukkit.getWorlds;

public class SyncTask extends BukkitRunnable {

    private SilhouettePlugin plugin;
    private BukkitCraftPlayer craftPlayer;

    private Map<Integer, Location> playerPositions;

    private int viewDistance;

    SyncTask(SilhouettePlugin plugin, BukkitCraftPlayerFactory bukkitCraftPlayerFactory) {
        this.plugin = plugin;

        playerPositions = new HashMap<>();

        craftPlayer = bukkitCraftPlayerFactory.getBukkitCraftPlayer(plugin.getServer());

        viewDistance = getWorlds().get(0).getViewDistance();
        plugin.getLogger().info(String.format("view distance = %d", viewDistance));
    }

    @Override
    public void run() {
        for(Player player : plugin.getServer().getOnlinePlayers()) {
//            getLogger().info(String.format("updating player uuid=%s entityid=%d", player.getUniqueId(), player.getEntityId()));
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
//                getLogger().info(String.format("distance between players = %s", distance));
                if (distance > 160) {
                    if (!player.equals(otherPlayer)) {
//                        getLogger().info(String.format("sending position of other player uuid=%s entityid=%d", otherPlayer.getUniqueId(), otherPlayer.getEntityId()));
                        updatePlayer(player, otherPlayer);
                    }
                }
            }
        }
    }

    private void updatePlayer(Player player, Player otherPlayer) {

        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
//            craftPlayer.sendPacket(player, buildSpawnPlayerPacket(otherPlayer));
            craftPlayer.sendPacket(player, buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            craftPlayer.sendPacket(player, buildHeadLookPacket(otherPlayer));
            playerPositions.put(otherPlayer.getEntityId(), currentLocation);
        }
    }

    private PacketPlayOutNamedEntitySpawn buildSpawnPlayerPacket(Player player) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.d(player.getEntityId());
        serializer.a(player.getUniqueId());
        serializer.writeDouble(player.getLocation().getX());
        serializer.writeDouble(player.getLocation().getY());
        serializer.writeDouble(player.getLocation().getZ());
        serializer.writeByte(toAngle(player.getLocation().getYaw()));
        serializer.writeByte(toAngle(player.getLocation().getPitch()));
        return new PacketPlayOutNamedEntitySpawn(serializer);
    }

    private PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook buildMoveLookPacket(Player player, Location previousLocation, Location currentLocation) {
        if (previousLocation == null) {
            previousLocation = currentLocation;
        }
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.d(player.getEntityId());
        serializer.writeShort(calculateDelta(previousLocation.getX(), currentLocation.getX()));
        serializer.writeShort(calculateDelta(previousLocation.getY(), currentLocation.getY()));
        serializer.writeShort(calculateDelta(previousLocation.getZ(), currentLocation.getZ()));
        serializer.writeByte(toAngle(player.getLocation().getYaw()));
        serializer.writeByte(toAngle(player.getLocation().getPitch()));
        serializer.writeBoolean(player.isOnGround());  // FIXME this could cause issues
        return PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook.b(serializer);
    }

    // "While sending the Entity Look packet changes the vertical rotation of the head, sending this packet appears to be necessary to rotate the head horizontally."
    private PacketPlayOutEntityHeadRotation buildHeadLookPacket(Player player) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.writeInt(player.getEntityId());
        serializer.writeByte(toAngle(player.getLocation().getYaw()));
        return new PacketPlayOutEntityHeadRotation(serializer);
    }

    private byte toAngle(float f) {
        return (byte)((int)(f * 256.0F / 360.0F));
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ();
    }

    private short calculateDelta(double previous, double current) {
        // Change in X position as (currentX * 32 - prevX * 32) * 128    (source: https://wiki.vg/Protocol#Entity_Position)
        return (short) ((current * 32 - previous * 32) * 128);
    }

    void onPlayerJoin(Player player) {
        plugin.getLogger().info(String.format("onPlayerJoin"));
        playerPositions.remove(player.getEntityId());
//        for(Player otherPlayer: appleSkinSpigotPlugin.getServer().getOnlinePlayers()) {
//            craftPlayer.sendPacket(otherPlayer, buildSpawnPlayerPacket(player));
//        }
    }

    void onPlayerLogOut(Player player) {
        plugin.getLogger().info(String.format("onPlayerLogOut eid=%d", player.getEntityId()));
        playerPositions.remove(player.getEntityId());
        // TODO send entity despawn for player
    }

}