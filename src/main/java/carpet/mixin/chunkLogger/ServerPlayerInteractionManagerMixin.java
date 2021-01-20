package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Redirect(method = "method_33542", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26125(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean setBlockToAir(World world, BlockPos pos) {
        try {
            CarpetClientChunkLogger.setReason("Player removed block");
            return world.method_26125(pos);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
