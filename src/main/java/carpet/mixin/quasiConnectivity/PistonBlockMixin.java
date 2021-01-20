package carpet.mixin.quasiConnectivity;

import carpet.CarpetSettings;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "shouldExtend", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"), cancellable = true)
    private void quasiConnectivity(World worldIn, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetSettings.quasiConnectivity) cir.setReturnValue(false);
    }
}
