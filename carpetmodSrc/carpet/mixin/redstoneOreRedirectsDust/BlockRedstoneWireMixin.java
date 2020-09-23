package carpet.mixin.redstoneOreRedirectsDust;

import carpet.CarpetSettings;
import carpet.helpers.RedstoneOreRedirectHelper;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRedstoneWire.class)
public class BlockRedstoneWireMixin {
    @Inject(method = "getWeakPower", at = @At("HEAD"), cancellable = true)
    private void getWeakPowerFromOre(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.redstoneOreRedirectsDust) {
            cir.setReturnValue(RedstoneOreRedirectHelper.getWeakPowerCM((BlockRedstoneWire) (Object) this, blockState, blockAccess, pos, side));
        }
    }

    @Inject(method = "canConnectTo", at = @At("HEAD"), cancellable = true)
    private static void canConnectToOre(IBlockState blockState, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.redstoneOreRedirectsDust) {
            cir.setReturnValue(RedstoneOreRedirectHelper.canConnectToCM(blockState, side));
        }
    }
}
