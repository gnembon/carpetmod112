package narcolepticfrog.rsmm.clock;

import net.minecraft.util.PacketByteBuf;

public class SubtickTime implements Comparable<SubtickTime> {

    private long tick;
    private int subtickIndex;

    public SubtickTime(long tick, int subtickIndex) {
        this.tick = tick;
        this.subtickIndex = subtickIndex;
    }

    public long getTick() {
        return tick;
    }

    public int getSubtickIndex() {
        return subtickIndex;
    }

    @Override
    public int compareTo(SubtickTime o) {
        if (this.tick != o.tick) {
            return Long.compare(this.tick, o.tick);
        } else {
            return Integer.compare(this.subtickIndex, o.subtickIndex);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof SubtickTime) {
            SubtickTime ot = (SubtickTime) o;
            return compareTo(ot) == 0;
        }
        return false;
    }

    public String toString() {
        return "SubtickTime[tick = " + tick + ", subtickIndex = " + subtickIndex + "]";
    }

    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeVarLong(tick);
        buffer.writeInt(subtickIndex);
    }

    public static SubtickTime readFromBuffer(PacketByteBuf buffer) {
        long tick = buffer.readVarLong();
        int subtickIndex = buffer.readInt();
        return new SubtickTime(tick, subtickIndex);
    }

}
