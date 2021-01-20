package narcolepticfrog.rsmm.network;

import net.minecraft.util.PacketByteBuf;

/**
 * Base class for all client-bound packets in the RSMM mod.
 */
public abstract class RSMMCPacket {

    public abstract PacketByteBuf toBuffer();

    public abstract void process(RSMMCPacketHandler handler);

    public static RSMMCPacket fromBuffer(PacketByteBuf buffer) {
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
