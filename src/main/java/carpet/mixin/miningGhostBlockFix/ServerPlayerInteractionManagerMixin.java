package carpet.mixin.miningGhostBlockFix;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientServer;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow public World field_31754;
    @Shadow private BlockPos field_31759;

    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26094(ILnet/minecraft/util/math/BlockPos;I)V"))
    private void miningGhostBlockFix(BlockPos pos, Direction side, CallbackInfo ci) {
        if (CarpetSettings.miningGhostBlocksFix && CarpetClientServer.activateInstantMine) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(field_31754, field_31759));
        }
    }
}
