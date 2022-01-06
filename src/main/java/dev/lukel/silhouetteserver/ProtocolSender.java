package dev.lukel.silhouetteserver;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.lukel.silhouetteserver.packet.IPacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class ProtocolSender {

    private final ProtocolManager protocolManager;

    public ProtocolSender() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void sendPacket(Player receiver, IPacketContainer packet) {
        try {
            protocolManager.sendServerPacket(receiver, packet.getPacket());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("silhouette cannot send packet " + packet, e);
        }
    }

}
