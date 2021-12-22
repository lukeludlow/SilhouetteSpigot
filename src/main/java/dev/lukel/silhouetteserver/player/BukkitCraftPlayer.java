package dev.lukel.silhouetteserver.player;

import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

public interface BukkitCraftPlayer {

    void sendPacket(Player player, Packet<?> packet);

}