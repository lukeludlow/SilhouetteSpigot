package dev.lukel.silhouetteserver;

import dev.lukel.silhouetteserver.player.BukkitCraftPlayerFactory;
import org.bukkit.plugin.java.JavaPlugin;

public class SilhouettePlugin extends JavaPlugin {

    private SyncTask syncTask = null;
    private ProtocolLibraryAccessor protocolLibraryAccessor = null;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("silhouette onEnable");
        syncTask = createSyncTask();
        protocolLibraryAccessor = createProtocolLibraryAccessor();
        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        syncTask.runTaskTimer(this, 0L, 1L);
        protocolLibraryAccessor.listenToPackets();
    }

    SyncTask createSyncTask() {
        return new SyncTask(this, new BukkitCraftPlayerFactory());
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
