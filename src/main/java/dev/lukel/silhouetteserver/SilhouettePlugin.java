package dev.lukel.silhouetteserver;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

public class SilhouettePlugin extends JavaPlugin {

    private SyncTask syncTask = null;
    private ProtocolLibraryAccessor protocolLibraryAccessor = null;

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
        protocolLibraryAccessor = createProtocolLibraryAccessor();
        syncTask = createSyncTask();
        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        // delay=0, period=1 (run task every 1 server tick)
        syncTask.runTaskTimer(this, 0L, 1L);
        protocolLibraryAccessor.listenToPackets();
    }

    SyncTask createSyncTask() {
        return new SyncTask(this, protocolLibraryAccessor, new PacketBuilder(), new ProtocolSender());
    }

    ProtocolLibraryAccessor createProtocolLibraryAccessor() {
        return new ProtocolLibraryAccessor(this);
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
