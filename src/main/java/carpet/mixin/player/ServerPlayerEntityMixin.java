package carpet.mixin.player;

import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.IPlayerSensitiveTileEntity;
import carpet.utils.extensions.ActionPackOwner;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ActionPackOwner {
    private final EntityPlayerActionPack actionPack = new EntityPlayerActionPack((ServerPlayerEntity) (Object) this);

    @Override
    public EntityPlayerActionPack getActionPack() {
        return actionPack;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        actionPack.onUpdate();
    }

    @Redirect(method = "sendBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;toUpdatePacket()Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;"))
    private BlockEntityUpdateS2CPacket getUpdatePacket(BlockEntity blockEntity) {
        if (blockEntity instanceof IPlayerSensitiveTileEntity) {
            return ((IPlayerSensitiveTileEntity) blockEntity).getUpdatePacketPlayerSensitive((ServerPlayerEntity) (Object) this);
        }
        return blockEntity.toUpdatePacket();
    }
}
