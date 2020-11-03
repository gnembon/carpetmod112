package carpet.worldedit;

import java.nio.charset.Charset;

import com.sk89q.worldedit.LocalSession;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public static void onCustomPayload(CPacketCustomPayload rawPacket, EntityPlayerMP player) {
        if (rawPacket.getChannelName().equals(CarpetWorldEdit.CUI_PLUGIN_CHANNEL)) {
            LocalSession session = CarpetWorldEdit.inst.getSession((EntityPlayerMP) player);

            if (session.hasCUISupport()) {
                return;
            }
        
            PacketBuffer buff = rawPacket.getBufferData();
            buff.resetReaderIndex();
            byte[] bytes = new byte[buff.readableBytes()];
            buff.readBytes(bytes);
            String text = new String(bytes, UTF_8_CHARSET);
            session.handleCUIInitializationMessage(text);
        }
    }

}