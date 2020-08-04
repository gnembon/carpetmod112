package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockChest.class)
public class BlockChestMixin {
    @Redirect(method = {
            "getBoundingBox",
            "onBlockAdded",
            "checkForSurroundingChests",
            "isDoubleChest",
            "getContainer"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState onGetBoundingBox(IBlockAccess world, BlockPos pos) {
        return CarpetClientChunkLogger.getBlockState(world, pos, "Chest loading");
    }
}
