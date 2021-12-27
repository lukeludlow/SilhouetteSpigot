package dev.lukel.silhouetteserver;

import com.comphenix.protocol.events.PacketContainer;

public record IPacketContainer(PacketContainer packet) {

    public PacketContainer getPacket() {
        return packet;
    }

}
