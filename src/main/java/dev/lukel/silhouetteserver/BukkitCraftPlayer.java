package dev.lukel.silhouetteserver;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import org.bukkit.entity.Player;

public interface BukkitCraftPlayer {

    void sendPacket(Player player, Packet<?> packet);

}