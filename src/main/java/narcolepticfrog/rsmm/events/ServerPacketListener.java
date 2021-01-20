package narcolepticfrog.rsmm.events;

import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public interface ServerPacketListener {

    void onCustomPayload(ServerPlayerEntity sender, String channel, PacketByteBuf data);

    void onChannelRegister(ServerPlayerEntity sender, List<String> channels);

    void onChannelUnregister(ServerPlayerEntity sender, List<String> channels);

}
