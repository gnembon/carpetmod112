package narcolepticfrog.rsmm.network;

import io.netty.buffer.Unpooled;
import narcolepticfrog.rsmm.DimPos;
import net.minecraft.network.PacketBuffer;

/**
 * This packet is sent from the client to the server indicating that a meter should be toggled
 * at the provided DimPos.
 */
public class RSMMSPacketToggleMeter extends RSMMSPacket {

    public static final byte MESSAGE_ID = 0;

    private DimPos dimpos;
    private boolean movable;

    public RSMMSPacketToggleMeter(DimPos dimpos, boolean movable) {
        this.dimpos = dimpos;
        this.movable = movable;
    }

    public DimPos getDimpos() {
        return dimpos;
    }

    public boolean isMovable() {
        return movable;
    }

    public static RSMMSPacketToggleMeter fromBuffer(PacketBuffer buffer) {
        Byte messageId = buffer.readByte();
        assert messageId == MESSAGE_ID;
        DimPos dimpos = DimPos.readFromBuffer(buffer);
        boolean movable = buffer.readBoolean();
        return new RSMMSPacketToggleMeter(dimpos, movable);
    }

    @Override
    public PacketBuffer toBuffer() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(MESSAGE_ID);
        dimpos.writeToBuffer(buffer);
        buffer.writeBoolean(movable);
        return buffer;
    }

    @Override
    public void process(RSMMSPacketHandler handler) {
        handler.handleToggleMeter(this);
    }

}
