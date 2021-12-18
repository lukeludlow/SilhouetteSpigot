package dev.lukel.silhouetteserver;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.getWorlds;

public class SyncTask extends BukkitRunnable {

    private final MinecraftKey SATURATION_KEY = new MinecraftKey("silhouette", "saturation_sync");
    private final MinecraftKey EXHAUSTION_KEY = new MinecraftKey("silhouette", "exhaustion_sync");
    private final float MINIMUM_EXHAUSTION_CHANGE_THRESHOLD = 0.01F;

    private SilhouettePlugin appleSkinSpigotPlugin;
    private BukkitCraftPlayer craftPlayer;
    private Map<UUID, Float> previousSaturationLevels;
    private Map<UUID, Float> previousExhaustionLevels;

    private Map<Integer, Location> playerPositions;

    private int viewDistance;

    private int i;
    private final int i_limit = 50;


    SyncTask(SilhouettePlugin appleSkinSpigotPlugin, BukkitCraftPlayerFactory bukkitCraftPlayerFactory) throws UnsupportedMinecraftVersionException {
        i = 0;
        this.appleSkinSpigotPlugin = appleSkinSpigotPlugin;
        previousSaturationLevels = new HashMap<>();
        previousExhaustionLevels = new HashMap<>();

        playerPositions = new HashMap<>();

        craftPlayer = bukkitCraftPlayerFactory.getBukkitCraftPlayer(appleSkinSpigotPlugin.getServer());
        if(craftPlayer == null) {
            throw new UnsupportedMinecraftVersionException();
        }


        viewDistance = getWorlds().get(0).getViewDistance();
        getLogger().info(String.format("view distance = %d", viewDistance));
    }

    @Override
    public void run() {
        appleSkinSpigotPlugin.getServer().getOnlinePlayers();
        for(Player player : appleSkinSpigotPlugin.getServer().getOnlinePlayers()) {
//            getLogger().info(String.format("updating player uuid=%s entityid=%d", player.getUniqueId(), player.getEntityId()));
            for (Player otherPlayer : appleSkinSpigotPlugin.getServer().getOnlinePlayers()) {
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
        i++;
        if (i > i_limit) {
            i = 0;
        }
    }

    private void updatePlayer(Player player, Player otherPlayer) {

        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
            if (i == i_limit) {
                getLogger().info("i_limit");
//                craftPlayer.sendPacket(player, buildSpawnPlayerPacket(otherPlayer));
            }
            craftPlayer.sendPacket(player, buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            craftPlayer.sendPacket(player, buildHeadLookPacket(otherPlayer));
//            craftPlayer.sendPacket(player, buildPositionPacket(otherPlayer, previousLocation, currentLocation));
            playerPositions.put(otherPlayer.getEntityId(), currentLocation);
        }



//        float saturation = player.getSaturation();
//        Float previousSaturation = previousSaturationLevels.get(player.getUniqueId());
//        if(previousSaturation == null || saturation != previousSaturation) {
//            craftPlayer.sendPacket(player, buildPacket(SATURATION_KEY, saturation));
//            previousSaturationLevels.put(player.getUniqueId(), saturation);
//        }
//
//        player.getLocation().getX();
//
//        float exhaustion = player.getExhaustion();
//        Float previousExhaustion = previousExhaustionLevels.get(player.getUniqueId());
//        if(previousExhaustion == null || Math.abs(exhaustion - previousExhaustion) >= MINIMUM_EXHAUSTION_CHANGE_THRESHOLD) {
//            craftPlayer.sendPacket(player, buildPacket(EXHAUSTION_KEY, exhaustion));
//            previousExhaustionLevels.put(player.getUniqueId(), exhaustion);
//        }

    }

    private PacketPlayOutNamedEntitySpawn buildSpawnPlayerPacket(Player player) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.d(player.getEntityId());
        serializer.a(player.getUniqueId());
        serializer.writeDouble(player.getLocation().getX());
        serializer.writeDouble(player.getLocation().getY());
        serializer.writeDouble(player.getLocation().getZ());
        // To get the real pitch, you must divide this by (256.0F / 360.0F)
//        serializer.writeByte((int) (player.getLocation().getYaw() / (256.0F / 360.0F)));
//        serializer.writeByte((int) (player.getLocation().getPitch() / (256.0F / 360.0F)));
//        serializer.writeByte((byte)((int)(player.getLocation().getYaw() * 256.0F / 360.0F)));
//        serializer.writeByte((byte)((int)(player.getLocation().getPitch() * 256.0F / 360.0F)));
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
//        serializer.writeShort((short)0);
//        serializer.writeShort((short)0);
//        serializer.writeShort((short)0);
        serializer.writeShort(calculateDelta(previousLocation.getX(), currentLocation.getX()));
        serializer.writeShort(calculateDelta(previousLocation.getY(), currentLocation.getY()));
        serializer.writeShort(calculateDelta(previousLocation.getZ(), currentLocation.getZ()));
        serializer.writeByte(toAngle(player.getLocation().getYaw()));
        serializer.writeByte(toAngle(player.getLocation().getPitch()));
        serializer.writeBoolean(player.isOnGround());  // FIXME this could cause issues
        return PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook.b(serializer);
    }

    private PacketPlayOutEntityHeadRotation buildHeadLookPacket(Player player) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.writeInt(player.getEntityId());
        serializer.writeByte(toAngle(player.getLocation().getYaw()));
        return new PacketPlayOutEntityHeadRotation(serializer);
    }

    // "While sending the Entity Look packet changes the vertical rotation of the head, sending this packet appears to be necessary to rotate the head horizontally."

    private byte toAngle(float f) {
        return (byte)((int)(f * 256.0F / 360.0F));
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ();
    }

//    private PacketPlayOutEntity.PacketPlayOutRelEntityMove buildPositionPacket(Player player, Location previousLocation, Location currentLocation) {
//        if (previousLocation == null) {
//            previousLocation = currentLocation;
//        }
//        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
//        serializer.writeInt(player.getEntityId());
//        serializer.writeShort(calculateDelta(previousLocation.getX(), currentLocation.getX()));
//        serializer.writeShort(calculateDelta(previousLocation.getY(), currentLocation.getY()));
//        serializer.writeShort(calculateDelta(previousLocation.getZ(), currentLocation.getZ()));
//        serializer.writeBoolean(player.isOnGround());
//        return PacketPlayOutEntity.PacketPlayOutRelEntityMove.b(serializer);
//    }

    private short calculateDelta(double previous, double current) {
        // Change in X position as (currentX * 32 - prevX * 32) * 128    (source: https://wiki.vg/Protocol#Entity_Position)
        return (short) ((current * 32 - previous * 32) * 128);
    }

    void onPlayerLogIn(Player player) {
        getLogger().info(String.format("onPlayerLogIn"));
        playerPositions.remove(player.getEntityId());
        for(Player otherPlayer: appleSkinSpigotPlugin.getServer().getOnlinePlayers()) {
            craftPlayer.sendPacket(otherPlayer, buildSpawnPlayerPacket(player));
        }
    }

    void onPlayerLogOut(Player player) {
        getLogger().info(String.format("onPlayerLogOut eid=%d", player.getEntityId()));
        playerPositions.remove(player.getEntityId());
    }

}