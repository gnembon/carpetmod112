package carpet.mixin.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "updateNeighbor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkUpdateSuppression1(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci, BlockState iblockstate, Throwable throwable) {
        if (CarpetSettings.updateSuppressionCrashFix && (throwable instanceof ThrowableSuppression || throwable instanceof StackOverflowError)) {
            throw new ThrowableSuppression("Update suppression");
        }
    }
    @Inject(method = "onBlockChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkUpdateSuppression2(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci, BlockState iblockstate, Throwable throwable) {
        if (CarpetSettings.updateSuppressionCrashFix && (throwable instanceof ThrowableSuppression || throwable instanceof StackOverflowError)) {
            throw new ThrowableSuppression("Update suppression");
        }
    }
}
