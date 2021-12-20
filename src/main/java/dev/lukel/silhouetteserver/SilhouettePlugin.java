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


public class SilhouettePlugin extends JavaPlugin {

    private SyncTask syncTask = null;
//    private ProtocolLibraryAccessor protocolLibraryAccessor = null;

    @Override
    public void onEnable() {
        super.onEnable();
//        getLogger().info("silhouette onEnable");
        try {
//            protocolLibraryAccessor = new ProtocolLibraryAccessor();
            syncTask = createSyncTask();
        } catch (UnsupportedMinecraftVersionException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        syncTask.runTaskTimer(this, 0L, 1L);

//        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        ProtocolManager protocolManager = ProtocolLibraryAccessor.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_DESTROY) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY) {
                            PacketContainer packet = event.getPacket();
//                            getLogger().info("received entity destroy packet");
                            StructureModifier<List<Integer>> entityIdIntLists= packet.getIntLists();
                            entityIdIntLists.getValues().forEach(list -> {
                                list.forEach(entityId -> {
                                    if (getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
//                                        getLogger().info("cancelling packet");
                                        event.setCancelled(true);
                                    }
                                });
                            });
                        }
                    }
                }
        );

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
                            PacketContainer packet = event.getPacket();
                            StructureModifier<Object> modifier = packet.getModifier();
                            int entityId = (int) modifier.readSafely(0);
                            if (getServer().getOnlinePlayers().stream().anyMatch(player -> player.getEntityId() == entityId)) {
//                                getLogger().info(String.format("rel entity move look packet. player entityId = %d", entityId));
                            }
                        }
                    }
                }
        );

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
                            PacketContainer packet = event.getPacket();
                        }
                    }
                }
        );

    }

    SyncTask createSyncTask() throws UnsupportedMinecraftVersionException {
        return new SyncTask(this, new BukkitCraftPlayerFactory());
    }

    @Override
    public void onDisable() {
        super.onDisable();
//        getLogger().info("silhouette onDisable");
        if(syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }
}
