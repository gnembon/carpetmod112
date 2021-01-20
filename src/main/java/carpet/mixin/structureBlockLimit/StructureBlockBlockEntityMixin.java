package carpet.mixin.structureBlockLimit;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientServer;
import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin extends BlockEntity implements IPlayerSensitiveTileEntity {
    @Shadow private BlockPos size;

    @ModifyConstant(method = "fromTag", constant = {@Constant(intValue = -32), @Constant(intValue = 32)})
    private int structureBlockLimit(int origValue) {
        return origValue < 0 ? -CarpetSettings.structureBlockLimit : CarpetSettings.structureBlockLimit;
    }

    // Make sure the rendering isn't messed up for non-carpet-client clients when size is greater than vanilla limit
    @Override
    public BlockEntityUpdateS2CPacket getUpdatePacketPlayerSensitive(ServerPlayerEntity player) {
        CompoundTag updateTag = toInitialChunkDataTag();
        if (!CarpetClientServer.isPlayerRegistered(player) && (size.getX() > 32 || size.getY() > 32 || size.getZ() > 32)) {
            updateTag.putInt("sizeX", 0);
            updateTag.putInt("sizeY", 0);
            updateTag.putInt("sizeZ", 0);
        }
        return new BlockEntityUpdateS2CPacket(pos, 7, updateTag);
    }
}
