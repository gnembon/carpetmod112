package carpet.mixin.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkUpdateSuppression1(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci, IBlockState iblockstate, Throwable throwable) {
        if (CarpetSettings.updateSuppressionCrashFix && (throwable instanceof ThrowableSuppression || throwable instanceof StackOverflowError)) {
            throw new ThrowableSuppression("Update suppression");
        }
    }
    @Inject(method = "observedNeighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkUpdateSuppression2(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci, IBlockState iblockstate, Throwable throwable) {
        if (CarpetSettings.updateSuppressionCrashFix && (throwable instanceof ThrowableSuppression || throwable instanceof StackOverflowError)) {
            throw new ThrowableSuppression("Update suppression");
        }
    }
}
