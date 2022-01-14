package dev.lukel.silhouetteserver;

import dev.lukel.silhouetteserver.packet.IPacketContainer;
import dev.lukel.silhouetteserver.packet.PacketBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncTask extends BukkitRunnable {

    private final SilhouettePlugin plugin;
    private final ProtocolListener protocolListener;
    private final PacketBuilder packetBuilder;
    private final ProtocolSender protocolSender;

    private final double viewDistanceBlockRange;

    private final Map<Integer, Location> playerPositions;
    private final Map<Integer, Pose> playerPoses;

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
        // update maps after the loop (otherwise first update "eats" update before the other people)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playerPositions.put(player.getEntityId(), player.getLocation());
            playerPoses.put(player.getEntityId(), player.getPose());
        }
    }

    public void updatePlayer(Player player, Player otherPlayer) {
        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        Pose currentPose = otherPlayer.getPose();
        Pose previousPose = playerPoses.get(otherPlayer.getEntityId());
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
            if (previousLocation == null) {
                previousLocation = currentLocation;
            }
            protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
        }
        if (currentPose != previousPose) {
            protocolSender.sendPacket(player, packetBuilder.buildEntityMetadataPacket(otherPlayer));
        }
    }

    void updatePlayerEquipment(Player player) {
        // notify all other players what equipment this player has
        final long delay = 1;  // (wait one tick so that the item/inventory click/move event finishes)
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (shouldSendUpdate(player, otherPlayer)) {
                    updatePlayerEquipment(otherPlayer, player);
                }
            }
        }, delay);
    }

    private void updatePlayerEquipment(Player receiver, Player subject) {
        List<IPacketContainer> equipmentPackets = packetBuilder.buildPlayerEquipmentPackets(subject);
        equipmentPackets.forEach(packet -> {
            protocolSender.sendPacket(receiver, packet);
        });
    }

    void onPlayerAnimation(Player player) {
        // note that PlayerAnimationEvent's PlayerAnimationType is only ARM_SWING so no logic here
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            if (shouldSendUpdate(player, otherPlayer)) {
                protocolSender.sendPacket(otherPlayer, packetBuilder.buildPlayerAnimationPacket(player));
            }
        }
    }

    void onPlayerJoin(Player player) {
        playerPositions.remove(player.getEntityId());
        playerPoses.remove(player.getEntityId());
        spawnPlayerForEveryoneElse(player);
    }

    void onPlayerLogOut(Player player) {
        playerPositions.remove(player.getEntityId());
        playerPoses.remove(player.getEntityId());
        destroyPlayerForEveryoneElse(player);
    }

    void onPlayerRespawn(Player player) {
        spawnPlayerForEveryoneElse(player);
    }

    void onPlayerChangedWorld(Player player) {
        destroyPlayerForEveryoneElse(player);
        spawnPlayerForEveryoneElse(player, 20);
    }

    void onPlayerDeath(Player player) {
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

    void spawnPlayerForEveryoneElse(Player player) {
        final long delay = 1;  // delay in server ticks before executing task
        spawnPlayerForEveryoneElse(player, delay);
    }

    void spawnPlayerForEveryoneElse(Player player, long delay) {
        // need to schedule this to run later so that PlayerInfo is sent to all players before the spawn player packet
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // notify other players that this new player joined
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (isSameDimension(player, otherPlayer) && isNotSamePerson(player, otherPlayer)) {
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildPlayerSpawnPacket(player));
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildMoveLookPacket(player, player.getLocation(), player.getLocation()));
                    protocolSender.sendPacket(otherPlayer, packetBuilder.buildHeadLookPacket(player));
                    updatePlayerEquipment(otherPlayer, player);
                }
            }
            // notify the joining player where everyone else is
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (isSameDimension(player, otherPlayer) && isNotSamePerson(player, otherPlayer)) {
                    protocolSender.sendPacket(player, packetBuilder.buildPlayerSpawnPacket(otherPlayer));
                    protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, otherPlayer.getLocation(), otherPlayer.getLocation()));
                    protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
                    updatePlayerEquipment(player, otherPlayer);
                }
            }
        }, delay);
    }

    private void destroyPlayerForEveryoneElse(Player player) {
        protocolListener.allowPlayerEntityDestroyPackets();
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            // destroy regardless of distance and dimension
            if (!player.equals(otherPlayer)) {
                protocolSender.sendPacket(otherPlayer, packetBuilder.buildDestroyEntityPacket(player));
            }
        }
        protocolListener.blockPlayerEntityDestroyPackets();
    }

    private boolean shouldSendUpdate(Player player1, Player player2) {
        return isSameDimension(player1, player2) &&
                isNotSamePerson(player1, player2) &&
                isPastRenderDistance(player1, player2);
    }

    private boolean isSameDimension(Player player1, Player player2) {
        return player1.getWorld().getEnvironment() == player2.getWorld().getEnvironment();
    }

    private boolean isNotSamePerson(Player player1, Player player2) {
        return !player1.equals(player2);
    }

    private boolean isPastRenderDistance(Player player1, Player player2) {
        return player1.getLocation().distance(player2.getLocation()) > viewDistanceBlockRange;
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ() ||
                previousLocation.getPitch() != currentLocation.getPitch() ||
                previousLocation.getYaw() != currentLocation.getYaw();
    }

}