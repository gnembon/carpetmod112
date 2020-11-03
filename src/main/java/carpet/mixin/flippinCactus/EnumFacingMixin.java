package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnumFacing.class)
public class EnumFacingMixin {
    @Inject(method = "getDirectionFromEntityLiving", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private static void flipDown(BlockPos pos, EntityLivingBase placer, CallbackInfoReturnable<EnumFacing> cir) {
        if (BlockRotator.flippinEligibility(placer)) cir.setReturnValue(EnumFacing.DOWN);
    }

    @Inject(method = "getDirectionFromEntityLiving", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void flipUp(BlockPos pos, EntityLivingBase placer, CallbackInfoReturnable<EnumFacing> cir) {
        if (BlockRotator.flippinEligibility(placer)) cir.setReturnValue(EnumFacing.UP);
    }
}
