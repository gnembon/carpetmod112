package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Redirect(method = {
            "getBoundingBox",
            "onBlockAdded",
            "method_26509",
            "method_26507",
            "method_26505"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState onGetBoundingBox(World world, BlockPos pos) {
        return CarpetClientChunkLogger.getBlockState(world, pos, "Chest loading");
    }
}
