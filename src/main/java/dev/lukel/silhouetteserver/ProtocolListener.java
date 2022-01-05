package dev.lukel.silhouetteserver;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
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

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        int entityId = packet.getIntegers().readSafely(0);
                        if (plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
                            StructureModifier<List<WrappedWatchableObject>> fuck = packet.getWatchableCollectionModifier();
                            List<WrappedWatchableObject> wrapped = fuck.readSafely(0);
                            WrappedWatchableObject w = wrapped.get(0);
//                            plugin.getLogger().info(String.format("w.getValue()=%s class=%s", w.getValue(), w.getValue().getClass()));
                            if (w.getValue() == net.minecraft.world.entity.EntityPose.c) {  // SLEEPING
                                plugin.getLogger().info(String.format("listened to entity metadata packet. entityId=%d w=%s", entityId, w));
                            }
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
