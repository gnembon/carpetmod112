package carpet.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;

public class NetHandlerPlayServerFake extends NetHandlerPlayServer
{
    public NetHandlerPlayServerFake(MinecraftServer server, NetworkManager nm, EntityPlayerMP playerIn)
    {
        super(server, nm, playerIn);
    }

    public void sendPacket(final Packet<?> packetIn)
    {
    }

    public void disconnect(String reason)
    {
    }
}



