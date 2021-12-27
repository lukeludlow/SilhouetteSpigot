package dev.lukel.silhouetteserver;

import com.comphenix.protocol.events.PacketContainer;

// this class exists just to enable unit testing, because importing a ProtocolLib class like PacketContainer
// in a unit test breaks everything
public record IPacketContainer(PacketContainer packet) {

    public PacketContainer getPacket() {
        return packet;
    }

}
