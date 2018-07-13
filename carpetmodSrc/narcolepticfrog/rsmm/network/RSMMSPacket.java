package narcolepticfrog.rsmm.network;

import net.minecraft.network.PacketBuffer;

/**
 * Base class for all server-bound packets in the RSMM mod.
 */
public abstract class RSMMSPacket {

    public abstract PacketBuffer toBuffer();

    public abstract void process(RSMMSPacketHandler handler);

    public static RSMMSPacket fromBuffer(PacketBuffer buffer) {
        byte messageId = buffer.getByte(0);

        switch(messageId) {

            case RSMMSPacketToggleMeter.MESSAGE_ID:
                return RSMMSPacketToggleMeter.fromBuffer(buffer);

        }

        return null;
    }

}
