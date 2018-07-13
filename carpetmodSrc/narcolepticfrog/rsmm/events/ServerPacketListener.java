package narcolepticfrog.rsmm.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public interface ServerPacketListener {

    void onCustomPayload(EntityPlayerMP sender, String channel, PacketBuffer data);

    void onChannelRegister(EntityPlayerMP sender, List<String> channels);

    void onChannelUnregister(EntityPlayerMP sender, List<String> channels);

}
