package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {
    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;getOpposite()Lnet/minecraft/util/math/Direction;"))
    private Direction flip(Direction facing, World worldIn, BlockPos pos, Direction facingArg, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        if (BlockRotator.flippinEligibility(placer)) return facing; // flipped twice
        return facing.getOpposite();
    }
}
