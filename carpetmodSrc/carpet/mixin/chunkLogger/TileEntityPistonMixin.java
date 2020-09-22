package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeTileEntity(Lnet/minecraft/util/math/BlockPos;)V"))
    private void removeTileEntityAndLog(World world, BlockPos pos) {
        CarpetClientChunkLogger.setReason("Piston block finishes moving");
        world.removeTileEntity(pos);
        CarpetClientChunkLogger.resetReason();
    }
}
