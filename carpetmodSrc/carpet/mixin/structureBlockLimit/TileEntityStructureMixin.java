package carpet.mixin.structureBlockLimit;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientServer;
import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileEntityStructure.class)
public abstract class TileEntityStructureMixin extends TileEntity implements IPlayerSensitiveTileEntity {
    @Shadow private BlockPos size;

    @ModifyConstant(method = "readFromNBT", constant = {@Constant(intValue = -32), @Constant(intValue = 32)})
    private int structureBlockLimit(int origValue) {
        return origValue < 0 ? -CarpetSettings.structureBlockLimit : CarpetSettings.structureBlockLimit;
    }

    // Make sure the rendering isn't messed up for non-carpet-client clients when size is greater than vanilla limit
    @Override
    public SPacketUpdateTileEntity getUpdatePacketPlayerSensitive(EntityPlayerMP player) {
        NBTTagCompound updateTag = getUpdateTag();
        if (!CarpetClientServer.isPlayerRegistered(player) && (size.getX() > 32 || size.getY() > 32 || size.getZ() > 32)) {
            updateTag.setInteger("sizeX", 0);
            updateTag.setInteger("sizeY", 0);
            updateTag.setInteger("sizeZ", 0);
        }
        return new SPacketUpdateTileEntity(pos, 7, updateTag);
    }
}
