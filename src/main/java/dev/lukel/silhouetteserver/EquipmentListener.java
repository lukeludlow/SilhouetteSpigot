package dev.lukel.silhouetteserver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class EquipmentListener implements Listener {

    private final SyncTask syncTask;

    EquipmentListener(SyncTask syncTask) {
        this.syncTask = syncTask;
    }

    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        syncTask.updatePlayerEquipment(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        syncTask.updatePlayerEquipment((Player)event.getWhoClicked());
    }

}
