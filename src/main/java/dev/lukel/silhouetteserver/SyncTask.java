package dev.lukel.silhouetteserver;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SyncTask extends BukkitRunnable {

    private final SilhouettePlugin plugin;
    private final ProtocolListener protocolListener;
    private final PacketBuilder packetBuilder;
    private final ProtocolSender protocolSender;

    private final Map<Integer, Location> playerPositions;
    private final int viewDistanceBlockRange;

    SyncTask(SilhouettePlugin plugin, ProtocolListener protocolListener, PacketBuilder packetBuilder, ProtocolSender protocolSender) {
        this.plugin = plugin;
        this.protocolListener = protocolListener;
        this.packetBuilder = packetBuilder;
        this.protocolSender = protocolSender;

        playerPositions = new HashMap<>();

        int viewDistance = plugin.getServer().getViewDistance();
        int blocksPerChunk = 16;
        viewDistanceBlockRange = viewDistance * blocksPerChunk;
        plugin.getLogger().info(String.format("set view distance block range to %d blocks", viewDistanceBlockRange));
    }

    @Override
    public void run() {
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
                if (distance > viewDistanceBlockRange) {
                    if (!player.equals(otherPlayer)) {
                        updatePlayer(player, otherPlayer);
                    }
                }
            }
        }
    }

    public void updatePlayer(Player player, Player otherPlayer) {

        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        // TODO send metadata packet even when not different position!
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
            if (previousLocation == null) {
                previousLocation = currentLocation;
            }
            protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
            protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            // TODO add action e.g. flying elytra or mining block or crouching
            playerPositions.put(otherPlayer.getEntityId(), currentLocation);
        }
        protocolSender.sendPacket(player, packetBuilder.buildEntityMetadataPacket(otherPlayer));
    }

    public void syncPlayerPosition() {
        // TODO
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ();
    }

    void onPlayerJoin(Player player) {
        playerPositions.remove(player.getEntityId());
        spawnPlayerForEveryoneElse(player);
    }

    void spawnPlayerForEveryoneElse(Player player) {
        // need to schedule this to run later so that PlayerInfo is sent to all players before the spawn player packet
        final long delay = 1;  // delay in server ticks before executing task
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // notify other players that this new player joined
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
                if (distance > viewDistanceBlockRange) {
                    if (!otherPlayer.equals(player)) {
                        protocolSender.sendPacket(otherPlayer, packetBuilder.buildPlayerSpawnPacket(player));
                        protocolSender.sendPacket(otherPlayer, packetBuilder.buildMoveLookPacket(player, player.getLocation(), player.getLocation()));
                        protocolSender.sendPacket(otherPlayer, packetBuilder.buildHeadLookPacket(player));
                    }
                }
            }
            // notify the joining player where everyone else is
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
                if (distance > viewDistanceBlockRange) {
                    if (!otherPlayer.equals(player)) {
                        protocolSender.sendPacket(player, packetBuilder.buildPlayerSpawnPacket(otherPlayer));
                        protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, otherPlayer.getLocation(), otherPlayer.getLocation()));
                        protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
                    }
                }
            }
        }, delay);
    }

    void onPlayerLogOut(Player player) {
        playerPositions.remove(player.getEntityId());
        protocolListener.allowPlayerEntityDestroyPackets();
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            protocolSender.sendPacket(otherPlayer, packetBuilder.buildDestroyEntityPacket(player));
        }
        final long delay = 1;  // delay in server ticks before executing task
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, protocolListener::blockPlayerEntityDestroyPackets, 1);
    }

}