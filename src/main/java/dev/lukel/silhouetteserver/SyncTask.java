package dev.lukel.silhouetteserver;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SyncTask extends BukkitRunnable {

    private final SilhouettePlugin plugin;
    private final ProtocolLibraryAccessor protocolAccessor;
    private final PacketBuilder packetBuilder;
    private final ProtocolSender protocolSender;

    private final Map<Integer, Location> playerPositions;
    private final int viewDistanceBlockRange = 160;  // assuming view distance = 10

    SyncTask(SilhouettePlugin plugin, ProtocolLibraryAccessor protocolAccessor, PacketBuilder packetBuilder, ProtocolSender protocolSender) {
        this.plugin = plugin;
        this.protocolAccessor = protocolAccessor;
        this.packetBuilder = packetBuilder;
        this.protocolSender = protocolSender;

        playerPositions = new HashMap<>();

//        craftPlayer = bukkitCraftPlayerFactory.getBukkitCraftPlayer(plugin.getServer());

        plugin.getLogger().info(String.format("view distance = %d", viewDistanceBlockRange));
    }

    @Override
    public void run() {
        for(Player player : plugin.getServer().getOnlinePlayers()) {
//            getLogger().info(String.format("updating player uuid=%s entityid=%d", player.getUniqueId(), player.getEntityId()));
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
//                getLogger().info(String.format("distance between players = %s", distance));
                if (distance > viewDistanceBlockRange) {
                    if (!player.equals(otherPlayer)) {
//                        getLogger().info(String.format("sending position of other player uuid=%s entityid=%d", otherPlayer.getUniqueId(), otherPlayer.getEntityId()));
                        updatePlayer(player, otherPlayer);
                    }
                }
            }
        }
    }

    public void updatePlayer(Player player, Player otherPlayer) {

        Location currentLocation = otherPlayer.getLocation();
        Location previousLocation = playerPositions.get(otherPlayer.getEntityId());
        if (previousLocation == null || isDifferentPosition(previousLocation, currentLocation)) {
            if (previousLocation == null) {
                previousLocation = currentLocation;
            }
            protocolSender.sendPacket(player, packetBuilder.buildMoveLookPacket(otherPlayer, previousLocation, currentLocation));
            protocolSender.sendPacket(player, packetBuilder.buildHeadLookPacket(otherPlayer));
            playerPositions.put(otherPlayer.getEntityId(), currentLocation);
        }
    }

    private boolean isDifferentPosition(Location previousLocation, Location currentLocation) {
        return previousLocation.getX() != currentLocation.getX() ||
                previousLocation.getY() != currentLocation.getY() ||
                previousLocation.getZ() != currentLocation.getZ();
    }

    void onPlayerJoin(Player player) {
        plugin.getLogger().info(String.format("onPlayerJoin"));
        playerPositions.remove(player.getEntityId());
        spawnPlayerForEveryoneElse(player);
    }

    void spawnPlayerForEveryoneElse(Player player) {
        // need to schedule this to run later so that PlayerInfo is sent to all players before the spawn player packet
        final long delay = 1;  // delay in server ticks before executing task
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getLogger().info("executing scheduleSyncDelayedTask");
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
        plugin.getLogger().info(String.format("onPlayerLogOut eid=%d", player.getEntityId()));
        playerPositions.remove(player.getEntityId());
        // TODO manually send entity despawn for player !! that should fix it
        // the protocol accessor never has to cancel this packet because the logged out player is not in getServer().getOnlinePlayers() !
        protocolAccessor.allowPlayerEntityDestroyPackets();
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            plugin.getLogger().info("onPlayerLogOut trying to delete entity");
            protocolSender.sendPacket(otherPlayer, packetBuilder.buildDestroyEntityPacket(player));
        }
        final long delay = 1;  // delay in server ticks before executing task
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, protocolAccessor::blockPlayerEntityDestroyPackets, 1);
    }

}