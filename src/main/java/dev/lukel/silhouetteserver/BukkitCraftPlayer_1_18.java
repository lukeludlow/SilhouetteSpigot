package dev.lukel.silhouetteserver;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getLogger;

public class BukkitCraftPlayer_1_18 implements BukkitCraftPlayer {

    @Override
    public void sendPacket(Player player, Packet<?> packet) {
//        getLogger().info(String.format("craftPlayer sendPacket player id=%d packet type = %s", player.getEntityId(), packet.getClass()));
        ((CraftPlayer)player).getHandle().b.a(packet);
    }

}