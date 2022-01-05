package dev.lukel.silhouetteserver;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SyncTask extends BukkitRunnable {

    private final SilhouettePlugin plugin;
    private final ProtocolListener protocolListener;
    private final PacketBuilder packetBuilder;
    private final ProtocolSender protocolSender;

    private final Map<Integer, Location> playerPositions;
    private final Map<Integer, Pose> playerPoses;
    private final double viewDistanceBlockRange;

    SyncTask(SilhouettePlugin plugin, ProtocolListener protocolListener, PacketBuilder packetBuilder, ProtocolSender protocolSender) {
        this.plugin = plugin;
        this.protocolListener = protocolListener;
        this.packetBuilder = packetBuilder;
        this.protocolSender = protocolSender;

        playerPositions = new HashMap<>();
        playerPoses = new HashMap<>();

        int viewDistance = plugin.getServer().getViewDistance();
        int blocksPerChunk = 16;
        viewDistanceBlockRange = viewDistance * blocksPerChunk;
        plugin.getLogger().info(String.format("silhouette setting view distance block range to %d blocks", (int)viewDistanceBlockRange));
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (shouldSendUpdate(player, otherPlayer)) {
                    updatePlayer(player, otherPlayer);
                }
            }
        }
    }

    public void updatePlayer(Player player, Player otherPlayer) {
        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        Pose previousPose = playerPoses.get(otherPlayer.getEntityId());
        Pose currentPose = otherPlayer.getPose();
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
            if (previousLocation == null) {
                previousLocation = currentLocation;
            }
            protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
            playerPositions.put(otherPlayer.getEntityId(), currentLocation);
        }

        if (currentPose != previousPose) {
            protocolSender.sendPacket(player, packetBuilder.buildEntityMetadataPacket(otherPlayer));
            playerPoses.put(otherPlayer.getEntityId(), currentPose);
        }
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ() ||
                previousLocation.getPitch() != currentLocation.getPitch() ||
                previousLocation.getYaw() != currentLocation.getY();
    }

    void onPlayerJoin(Player player) {
        playerPositions.remove(player.getEntityId());
        playerPoses.remove(player.getEntityId());
        playerPositions.put(player.getEntityId(), player.getLocation());
        playerPoses.put(player.getEntityId(), player.getPose());
        spawnPlayerForEveryoneElse(player);
    }

    void spawnPlayerForEveryoneElse(Player player) {
        // need to schedule this to run later so that PlayerInfo is sent to all players before the spawn player packet
        final long delay = 1;  // delay in server ticks before executing task
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // notify other players that this new player joined
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (shouldSendUpdate(player, otherPlayer)) {
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildPlayerSpawnPacket(player));
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildMoveLookPacket(player, player.getLocation(), player.getLocation()));
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildHeadLookPacket(player));
                }
            }
            // notify the joining player where everyone else is
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (shouldSendUpdate(player, otherPlayer)) {
                    protocolSender.sendPacket(player, packetBuilder.buildPlayerSpawnPacket(otherPlayer));
                    protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, otherPlayer.getLocation(), otherPlayer.getLocation()));
                    protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
                }
            }
        }, delay);
    }

    void onPlayerLogOut(Player player) {
        playerPositions.remove(player.getEntityId());
        playerPoses.remove(player.getEntityId());
        protocolListener.allowPlayerEntityDestroyPackets();
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            protocolSender.sendPacket(otherPlayer, packetBuilder.buildDestroyEntityPacket(player));
        }
        protocolListener.blockPlayerEntityDestroyPackets();
    }

    void onPlayerRespawn(Player player) {
        spawnPlayerForEveryoneElse(player);
    }

    void onPlayerDeath(Player player) {
        plugin.getLogger().info(String.format("onPlayerDeath. player location = %s", player.getLocation()));
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            // only update other players beyond normal render distance that this player died
            if (shouldSendUpdate(player, otherPlayer)) {
                // need this packet first to trigger death animation
                protocolSender.sendPacket(otherPlayer, packetBuilder.buildEntityZeroHealthPacket(player));
                // wait 20 ticks before destroying so that death animation has time to play
                final long delay = 20;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildDestroyEntityPacket(player));
                }, delay);
            }
        }
    }

    private boolean shouldSendUpdate(Player player1, Player player2) {
        double distance = player1.getLocation().distance(player2.getLocation());
        return (distance > viewDistanceBlockRange) && !player1.equals(player2);
    }

}