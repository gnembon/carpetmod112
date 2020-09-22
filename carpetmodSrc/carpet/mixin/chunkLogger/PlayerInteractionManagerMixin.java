package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {
    @Redirect(method = "removeBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean setBlockToAir(World world, BlockPos pos) {
        try {
            CarpetClientChunkLogger.setReason("Player removed block");
            return world.setBlockToAir(pos);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
