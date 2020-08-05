package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class)
public class WorldMixin {
    @Inject(method = "updateComparatorOutputLevel", at = @At("HEAD"), cancellable = true)
    private void onComparatorUpdate(BlockPos pos, Block block, CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities) {
            CarpetClientChunkLogger.setReason("Comparator updates & lazy tile entities");
            TileEntityOptimizer.updateComparatorsAndLazyTileEntities((World) (Object) this, pos, block);
            CarpetClientChunkLogger.resetReason();
            ci.cancel();
        }
    }
}
