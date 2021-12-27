package dev.lukel.silhouetteserver;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
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
                                    if (plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
                                        if (shouldCancelDestroyPackets) {
                                            event.setCancelled(true);
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
