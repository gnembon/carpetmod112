package carpet.mixin.structureBlockLimit;

import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_4615;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(class_4615.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean field_31800;
    @Shadow @Final private List<ServerPlayerEntity> field_31793;

    @Inject(method = "method_33563", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;toUpdatePacket()Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;"), cancellable = true)
    private void sendPlayerSensitiveBlockEntity(BlockEntity be, CallbackInfo ci) {
        if (be instanceof IPlayerSensitiveTileEntity) {
            if (field_31800) {
                for (ServerPlayerEntity player : field_31793) {
                    BlockEntityUpdateS2CPacket packet = ((IPlayerSensitiveTileEntity) be).getUpdatePacketPlayerSensitive(player);
                    if (packet != null) player.networkHandler.method_33624(packet);
                }
            }
            ci.cancel();
        }
    }
}
