package carpet.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
    public FakeServerPlayNetworkHandler(MinecraftServer server, ClientConnection nm, ServerPlayerEntity playerIn) {
        super(server, nm, playerIn);
    }

    @Override
    public void method_33624(final Packet<?> packetIn) {
    }

    @Override
    public void disconnect(Text textComponent) {
        player.kill();
    }
}



