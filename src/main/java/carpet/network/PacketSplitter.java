package carpet.network;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET = 1048576; // 32767 for c2s
    public static final int MAX_PAYLOAD_PER_PACKET = MAX_TOTAL_PER_PACKET - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE = 104876;

    private static final Map<Pair<EntityPlayerMP, String>, ReadingSession> readingSessions = new HashMap<>();

    public static void send(EntityPlayerMP player, String channel, PacketBuffer packet) {
        int len = packet.writerIndex();
        packet.resetReaderIndex();
        for (int offset = 0; offset < len; offset += MAX_PAYLOAD_PER_PACKET) {
            int thisLen = Math.min(len - offset, MAX_PAYLOAD_PER_PACKET);
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer(thisLen));
            buf.resetWriterIndex();
            if (offset == 0) buf.writeVarInt(len);
            buf.writeBytes(packet, thisLen);
            player.connection.sendPacket(new SPacketCustomPayload(channel, buf));
        }
        packet.release();
    }

    public static PacketBuffer receive(EntityPlayerMP player, CPacketCustomPayload message) {
        return receive(player, message, DEFAULT_MAX_RECEIVE_SIZE);
    }

    public static PacketBuffer receive(EntityPlayerMP player, CPacketCustomPayload message, int maxLength) {
        Pair<EntityPlayerMP, String> key = Pair.of(player, message.getChannelName());
        return readingSessions.computeIfAbsent(key, ReadingSession::new).receive(message.getBufferData(), maxLength);
    }

    private static class ReadingSession {
        private final Pair<EntityPlayerMP, String> key;
        private int expectedSize = -1;
        private PacketBuffer received;
        private ReadingSession(Pair<EntityPlayerMP, String> key) {
            this.key = key;
        }

        private PacketBuffer receive(PacketBuffer data, int maxLength) {
            if (expectedSize < 0) {
                expectedSize = data.readVarInt();
                if (expectedSize > maxLength) throw new IllegalArgumentException("Payload too large");
                received = new PacketBuffer(Unpooled.buffer(expectedSize));
            }
            received.writeBytes(data.readBytes(data.readableBytes()));
            if (received.writerIndex() >= expectedSize) {
                readingSessions.remove(key);
                return received;
            }
            return null;
        }
    }
}
