package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPortalForcer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Inject(method = "method_26721", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/NetherPortalBlock$AreaHelper;createPortal()V", shift = At.Shift.AFTER), expect = 2)
    private void clearOnSpawnPortal(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) ((ExtendedPortalForcer) ((ServerWorld) world).getPortalForcer()).clearHistoryCache();
    }

    @Inject(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26019(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", shift = At.Shift.AFTER), expect = 2)
    private void clearOnNeighborChange(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.portalCaching) ((ExtendedPortalForcer) ((ServerWorld) world).getPortalForcer()).clearHistoryCache();
    }
}
