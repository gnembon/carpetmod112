package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Direction.class)
public class DirectionMixin {
    @Inject(method = "method_31959", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private static void flipDown(BlockPos pos, LivingEntity placer, CallbackInfoReturnable<Direction> cir) {
        if (BlockRotator.flippinEligibility(placer)) cir.setReturnValue(Direction.DOWN);
    }

    @Inject(method = "method_31959", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void flipUp(BlockPos pos, LivingEntity placer, CallbackInfoReturnable<Direction> cir) {
        if (BlockRotator.flippinEligibility(placer)) cir.setReturnValue(Direction.UP);
    }
}
