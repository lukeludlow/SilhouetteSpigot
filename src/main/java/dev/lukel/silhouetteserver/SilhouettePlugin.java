package dev.lukel.silhouetteserver;

import dev.lukel.silhouetteserver.packet.PacketBuilder;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

public class SilhouettePlugin extends JavaPlugin {

    private SyncTask syncTask;
    private ProtocolListener protocolListener;

    public SilhouettePlugin() {
        super();
    }

    protected SilhouettePlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("silhouette onEnable");
        protocolListener = createProtocolListener();
        syncTask = createSyncTask();
        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        getServer().getPluginManager().registerEvents(new DeathRespawnListener(syncTask), this);
        getServer().getPluginManager().registerEvents(new EquipmentListener(syncTask), this);
        getServer().getPluginManager().registerEvents(new AnimationListener(syncTask), this);
        syncTask.runTaskTimer(this, 0L, 1L); // delay=0, period=1 (run task every 1 server tick)
        protocolListener.listenToPackets();
    }

    SyncTask createSyncTask() {
        return new SyncTask(this, protocolListener, createPacketBuilder(), createProtocolSender());
    }

    ProtocolListener createProtocolListener() {
        return new ProtocolListener(this);
    }

    PacketBuilder createPacketBuilder() {
        return new PacketBuilder();
    }

    ProtocolSender createProtocolSender() {
        return new ProtocolSender();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("silhouette onDisable");
        if(syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

}
