package carpet.helpers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public interface IPlayerSensitiveTileEntity
{
    SPacketUpdateTileEntity getUpdatePacketPlayerSensitive(EntityPlayerMP player);
}
