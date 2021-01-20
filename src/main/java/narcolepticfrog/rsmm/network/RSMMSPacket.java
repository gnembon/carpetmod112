package narcolepticfrog.rsmm.network;

import net.minecraft.util.PacketByteBuf;

/**
 * Base class for all server-bound packets in the RSMM mod.
 */
public abstract class RSMMSPacket {

    public abstract PacketByteBuf toBuffer();

    public abstract void process(RSMMSPacketHandler handler);

    public static RSMMSPacket fromBuffer(PacketByteBuf buffer) {
        byte messageId = buffer.getByte(0);

        switch(messageId) {

            case RSMMSPacketToggleMeter.MESSAGE_ID:
                return RSMMSPacketToggleMeter.fromBuffer(buffer);

        }

        return null;
    }

}
