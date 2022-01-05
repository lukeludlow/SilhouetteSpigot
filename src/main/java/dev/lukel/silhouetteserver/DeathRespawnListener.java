package dev.lukel.silhouetteserver;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathRespawnListener implements Listener {

    private final SilhouettePlugin plugin;
    private final SyncTask syncTask;

    DeathRespawnListener(SilhouettePlugin plugin, SyncTask syncTask) {
        this.plugin = plugin;
        this.syncTask = syncTask;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        syncTask.onPlayerRespawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        syncTask.onPlayerDeath(event.getEntity());
    }

}
