package dev.lukel.silhouetteserver.player;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BukkitCraftPlayer_1_18 implements BukkitCraftPlayer {

    @Override
    public void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer)player).getHandle().b.a(packet);
    }

}