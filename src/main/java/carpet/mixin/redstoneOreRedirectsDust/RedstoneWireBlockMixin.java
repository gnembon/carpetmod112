package carpet.mixin.redstoneOreRedirectsDust;

import carpet.CarpetSettings;
import carpet.helpers.RedstoneOreRedirectHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Inject(method = "getWeakRedstonePower", at = @At("HEAD"), cancellable = true)
    private void getWeakPowerFromOre(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.redstoneOreRedirectsDust) {
            cir.setReturnValue(RedstoneOreRedirectHelper.getWeakPowerCM((RedstoneWireBlock) (Object) this, blockState, blockAccess, pos, side));
        }
    }

    @Inject(method = "method_26764", at = @At("HEAD"), cancellable = true)
    private static void canConnectToOre(BlockState blockState, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.redstoneOreRedirectsDust) {
            cir.setReturnValue(RedstoneOreRedirectHelper.canConnectToCM(blockState, side));
        }
    }
}
