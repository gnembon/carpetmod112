package narcolepticfrog.rsmm.network;

import net.minecraft.network.PacketBuffer;

/**
 * Base class for all client-bound packets in the RSMM mod.
 */
public abstract class RSMMCPacket {

    public abstract PacketBuffer toBuffer();

    public abstract void process(RSMMCPacketHandler handler);

    public static RSMMCPacket fromBuffer(PacketBuffer buffer) {
        byte messageId = buffer.getByte(0);

        switch(messageId) {

            case RSMMCPacketMeter.MESSAGE_ID:
                return RSMMCPacketMeter.fromBuffer(buffer);

            case RSMMCPacketMeterGroup.MESSAGE_ID:
                return RSMMCPacketMeterGroup.fromBuffer(buffer);

        }

        return null;
    }

}
