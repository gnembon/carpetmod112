package carpet.worldedit;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import com.sk89q.worldedit.LocalSession;

class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

    public static void onCustomPayload(CustomPayloadC2SPacket rawPacket, ServerPlayerEntity player) {
        if (rawPacket.method_32939().equals(CarpetWorldEdit.CUI_PLUGIN_CHANNEL)) {
            LocalSession session = CarpetWorldEdit.inst.getSession(player);

            if (session.hasCUISupport()) {
                return;
            }
        
            PacketByteBuf buff = rawPacket.method_32941();
            buff.resetReaderIndex();
            byte[] bytes = new byte[buff.readableBytes()];
            buff.readBytes(bytes);
            String text = new String(bytes, UTF_8_CHARSET);
            session.handleCUIInitializationMessage(text);
        }
    }

}