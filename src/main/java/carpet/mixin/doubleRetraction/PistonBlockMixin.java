package carpet.mixin.doubleRetraction;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Shadow @Final public static BooleanProperty field_25225;

    @Inject(method = "method_27136", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1))
    private void onRetract(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (CarpetSettings.doubleRetraction) {
            worldIn.setBlockState(pos, state.with(field_25225, false), 2);
        }
    }
}
