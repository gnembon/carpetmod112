package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPortal.class)
public class BlockPortalMixin {
    @Inject(method = "trySpawnPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockPortal$Size;placePortalBlocks()V", shift = At.Shift.AFTER), expect = 2)
    private void clearOnSpawnPortal(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) ((WorldServer) world).getDefaultTeleporter().clearHistoryCache();
    }

    @Inject(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", shift = At.Shift.AFTER), expect = 2)
    private void clearOnNeighborChange(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.portalCaching) ((WorldServer) world).getDefaultTeleporter().clearHistoryCache();
    }
}
