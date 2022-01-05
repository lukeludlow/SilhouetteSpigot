package dev.lukel.silhouetteserver;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ProtocolListener {

    private final JavaPlugin plugin;
    private boolean shouldCancelDestroyPackets;

    public ProtocolListener(JavaPlugin plugin) {
        this.plugin = plugin;
        blockPlayerEntityDestroyPackets();
    }

    public void listenToPackets() {

        ProtocolManager protocolManager = getProtocolManager();

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_DESTROY) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY) {
                            PacketContainer packet = event.getPacket();
                            StructureModifier<List<Integer>> entityIdIntLists = packet.getIntLists();
                            entityIdIntLists.getValues().forEach(list -> {
                                list.forEach(entityId -> {
                                    Player foundPlayer = plugin.getServer().getOnlinePlayers().stream()
                                            .filter(player -> player.getEntityId() == entityId)
                                            .findFirst()
                                            .orElse(null);
                                    if (foundPlayer != null) {
                                        if (shouldCancelDestroyPackets) {
                                            if (foundPlayer.getHealth() == 0.0) {
                                                // player has died, so we DO want to send destroy packet
                                                plugin.getLogger().info("allowing destroy packet because player has died");
                                            } else {
                                                plugin.getLogger().info("cancelling player destroy packet");
                                                event.setCancelled(true);
                                            }
                                        }
                                    }
                                });
                            });
                        }
                    }
                }
        );

    }

    public void allowPlayerEntityDestroyPackets() {
        this.shouldCancelDestroyPackets = false;
    }

    public void blockPlayerEntityDestroyPackets() {
        this.shouldCancelDestroyPackets = true;
    }

    private ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

}
