package narcolepticfrog.rsmm.network;

import io.netty.buffer.Unpooled;
import narcolepticfrog.rsmm.DimPos;
import narcolepticfrog.rsmm.clock.SubtickTime;
import net.minecraft.network.PacketBuffer;

import java.util.BitSet;

public class RSMMCPacketMeter extends RSMMCPacket {

    public static final byte MESSAGE_ID = 0;

    private BitSet fields;
    private static final int METER_ID = 0;
    private static final int DIMPOS = 1;
    private static final int NAME = 2;
    private static final int COLOR = 3;
    private static final int TIME = 4;
    private static final int POWERED = 5;
    private static final int CREATE = 6;
    private static final int DELETE = 7;

    private static final int NUM_FIELDS = 8;

    private int meterId;
    private DimPos dimpos;
    private String name;
    private int color;
    private SubtickTime time;
    private boolean powered;
    private boolean movable;

    public void setMeterId(int meterId) {
        this.meterId = meterId;
        fields.set(METER_ID);
    }

    public int getMeterId() {
        return meterId;
    }

    public boolean hasMeterId() {
        return fields.get(METER_ID);
    }

    public void setDimpos(DimPos dimpos) {
        this.dimpos = dimpos;
        fields.set(DIMPOS);
    }

    public DimPos getDimpos() {
        return dimpos;
    }

    public boolean hasDimPos() {
        return fields.get(DIMPOS);
    }

    public void setName(String name) {
        this.name = name;
        fields.set(NAME);
    }

    public String getName() {
        return name;
    }

    public boolean hasName() {
        return fields.get(NAME);
    }

    public void setColor(int color) {
        this.color = color;
        fields.set(COLOR);
    }

    public int getColor() {
        return color;
    }

    public void setTime(SubtickTime time) {
        this.time = time;
        fields.set(TIME);
    }

    public boolean hasColor() {
        return fields.get(COLOR);
    }

    public SubtickTime getTime() {
        return time;
    }

    public boolean hasTime() {
        return fields.get(TIME);
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        fields.set(POWERED);
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean hasPowered() {
        return fields.get(POWERED);
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setCreate() {
        fields.set(CREATE);
    }

    public boolean shouldCreate() {
        return fields.get(CREATE);
    }

    public void setDelete() {
        fields.set(DELETE);
    }

    public boolean shouldDelete() {
        return fields.get(DELETE);
    }

    public RSMMCPacketMeter() {
        fields = new BitSet(NUM_FIELDS);
    }

    public static RSMMCPacketMeter fromBuffer(PacketBuffer buffer) {
        RSMMCPacketMeter packet = new RSMMCPacketMeter();

        Byte messageId = buffer.readByte();
        assert messageId == MESSAGE_ID;

        byte[] fieldArray = {buffer.readByte()};
        BitSet fields = BitSet.valueOf(fieldArray);

        if (fields.get(METER_ID)) packet.setMeterId(buffer.readInt());
        if (fields.get(DIMPOS)) packet.setDimpos(DimPos.readFromBuffer(buffer));
        if (fields.get(NAME)) packet.setName(buffer.readString(100));
        if (fields.get(COLOR)) packet.setColor(buffer.readInt());
        if (fields.get(TIME)) packet.setTime(SubtickTime.readFromBuffer(buffer));
        if (fields.get(POWERED)) packet.setPowered(buffer.readBoolean());
        if (fields.get(CREATE)) {
            packet.setMovable(buffer.readBoolean());
            packet.setCreate();
        }
        if (fields.get(DELETE)) packet.setDelete();

        return packet;
    }

    @Override
    public PacketBuffer toBuffer() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(MESSAGE_ID);
        buffer.writeByte(fields.toByteArray()[0]);

        if (fields.get(METER_ID)) buffer.writeInt(meterId);
        if (fields.get(DIMPOS)) dimpos.writeToBuffer(buffer);
        if (fields.get(NAME)) buffer.writeString(name);
        if (fields.get(COLOR)) buffer.writeInt(color);
        if (fields.get(TIME)) time.writeToBuffer(buffer);
        if (fields.get(POWERED)) buffer.writeBoolean(powered);
        if (fields.get(CREATE)) buffer.writeBoolean(movable);

        return buffer;
    }

    @Override
    public void process(RSMMCPacketHandler handler) {
        handler.handleMeter(this);
    }

}
