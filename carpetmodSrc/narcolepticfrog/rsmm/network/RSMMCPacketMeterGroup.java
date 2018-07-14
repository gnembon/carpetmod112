package narcolepticfrog.rsmm.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class RSMMCPacketMeterGroup extends RSMMCPacket {

    public static final byte MESSAGE_ID = 1;

     private String groupName;

    public RSMMCPacketMeterGroup(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public static RSMMCPacketMeterGroup fromBuffer(PacketBuffer buffer) {
        Byte messageId = buffer.readByte();
        assert messageId == MESSAGE_ID;
        String groupName = buffer.readString(100);
        return new RSMMCPacketMeterGroup(groupName);
    }

    @Override
    public PacketBuffer toBuffer() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(MESSAGE_ID);
        buffer.writeString(groupName);
        return buffer;
    }

    @Override
    public void process(RSMMCPacketHandler handler) {
        handler.handleMeterGroup(this);
    }

}
