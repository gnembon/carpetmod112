package carpet.mixin.player;

import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.IPlayerSensitiveTileEntity;
import carpet.utils.extensions.ActionPackOwner;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public class EntityPlayerMPMixin implements ActionPackOwner {
    private final EntityPlayerActionPack actionPack = new EntityPlayerActionPack((EntityPlayerMP) (Object) this);

    @Override
    public EntityPlayerActionPack getActionPack() {
        return actionPack;
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        actionPack.onUpdate();
    }

    @Redirect(method = "sendTileEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getUpdatePacket()Lnet/minecraft/network/play/server/SPacketUpdateTileEntity;"))
    private SPacketUpdateTileEntity getUpdatePacket(TileEntity blockEntity) {
        if (blockEntity instanceof IPlayerSensitiveTileEntity) {
            return ((IPlayerSensitiveTileEntity) blockEntity).getUpdatePacketPlayerSensitive((EntityPlayerMP) (Object) this);
        }
        return blockEntity.getUpdatePacket();
    }
}
