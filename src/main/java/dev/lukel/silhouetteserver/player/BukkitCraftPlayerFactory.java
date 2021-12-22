package dev.lukel.silhouetteserver.player;

import org.bukkit.Server;

public class BukkitCraftPlayerFactory {

    public BukkitCraftPlayer getBukkitCraftPlayer(Server server) {
        String version = server.getBukkitVersion();
        if (version.contains("1.18")) {
            return new BukkitCraftPlayer_1_18();
        } else {
            server.getLogger().warning(String.format("Unsupported server version: [%s]", version));
            return null;
        }
    }

}