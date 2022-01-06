package dev.lukel.silhouetteserver;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

public class AnimationListener implements Listener {

    private final SyncTask syncTask;

    AnimationListener(SyncTask syncTask) {
        this.syncTask = syncTask;
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        syncTask.onPlayerAnimation(event.getPlayer());
    }
}
