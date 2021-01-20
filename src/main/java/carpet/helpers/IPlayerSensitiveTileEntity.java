package carpet.helpers;

import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IPlayerSensitiveTileEntity
{
    BlockEntityUpdateS2CPacket getUpdatePacketPlayerSensitive(ServerPlayerEntity player);
}
