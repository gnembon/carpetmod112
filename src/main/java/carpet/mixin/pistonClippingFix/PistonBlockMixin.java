package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.PistonFixes;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "method_27136", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"))
    private void synchronize(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (CarpetSettings.pistonClippingFix > 0) PistonFixes.synchronizeClient();
    }
}
