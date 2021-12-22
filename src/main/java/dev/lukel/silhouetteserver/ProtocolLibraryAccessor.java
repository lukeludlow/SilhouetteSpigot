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

public class ProtocolLibraryAccessor {

    private JavaPlugin plugin;

    public ProtocolLibraryAccessor(JavaPlugin plugin) {
        this.plugin = plugin;
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
//                            String s = "online player entity ids: ";
//                            for (Player player : plugin.getServer().getOnlinePlayers()) {
//                                s += player.getEntityId() + ",";
//                            }
//                            plugin.getLogger().info("received entity destroy packet. " + s);
                            entityIdIntLists.getValues().forEach(list -> {
                                list.forEach(entityId -> {
//                                    for (Player player : plugin.getServer().getOnlinePlayers()) {
//                                        plugin.getLogger().info(String.format(
//                                                "player%d == entityId%d %s", player.getEntityId(), entityId, (player.getEntityId() == entityId ? "TRUE" : "")));
//                                    }
//                                    boolean anyMatch = plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId);
//                                    plugin.getLogger().info(String.format("filter entityId=%d, anyMatch=%b", entityId, anyMatch));
                                    if (plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
                                        plugin.getLogger().info("cancelling packet!!!");
                                        event.setCancelled(true);
                                    }
                                });
                            });
                        }
                    }
                }
        );

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
                            PacketContainer packet = event.getPacket();
                            StructureModifier<Object> modifier = packet.getModifier();
                            int entityId = (int) modifier.readSafely(0);
                            if (plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
//                                plugin.getLogger().info(String.format("rel entity move look packet. player entityId = %d", entityId));
                            }
                        }
                    }
                }
        );

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
                            PacketContainer packet = event.getPacket();
//                            plugin.getLogger().info(String.format("player info packet"));
                        }
                    }
                }
        );

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                            PacketContainer packet = event.getPacket();
//                            plugin.getLogger().info(String.format("named entity spawn"));
                        }
                    }
                }
        );
    }

    private ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

}
