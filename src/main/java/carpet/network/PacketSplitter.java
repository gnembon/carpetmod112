package carpet.network;

import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET = 1048576; // 32767 for c2s
    public static final int MAX_PAYLOAD_PER_PACKET = MAX_TOTAL_PER_PACKET - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE = 104876;

    private static final Map<Pair<ServerPlayerEntity, String>, ReadingSession> readingSessions = new HashMap<>();

    public static void send(ServerPlayerEntity player, String channel, PacketByteBuf packet) {
        int len = packet.writerIndex();
        packet.resetReaderIndex();
        for (int offset = 0; offset < len; offset += MAX_PAYLOAD_PER_PACKET) {
            int thisLen = Math.min(len - offset, MAX_PAYLOAD_PER_PACKET);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(thisLen));
            buf.resetWriterIndex();
            if (offset == 0) buf.writeVarInt(len);
            buf.writeBytes(packet, thisLen);
            player.networkHandler.method_33624(new CustomPayloadS2CPacket(channel, buf));
        }
        packet.release();
    }

    public static PacketByteBuf receive(ServerPlayerEntity player, CustomPayloadC2SPacket message) {
        return receive(player, message, DEFAULT_MAX_RECEIVE_SIZE);
    }

    public static PacketByteBuf receive(ServerPlayerEntity player, CustomPayloadC2SPacket message, int maxLength) {
        Pair<ServerPlayerEntity, String> key = Pair.of(player, message.method_32939());
        return readingSessions.computeIfAbsent(key, ReadingSession::new).receive(message.method_32941(), maxLength);
    }

    private static class ReadingSession {
        private final Pair<ServerPlayerEntity, String> key;
        private int expectedSize = -1;
        private PacketByteBuf received;
        private ReadingSession(Pair<ServerPlayerEntity, String> key) {
            this.key = key;
        }

        private PacketByteBuf receive(PacketByteBuf data, int maxLength) {
            if (expectedSize < 0) {
                expectedSize = data.readVarInt();
                if (expectedSize > maxLength) throw new IllegalArgumentException("Payload too large");
                received = new PacketByteBuf(Unpooled.buffer(expectedSize));
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
