package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockHopper.class)
public class BlockHopperMixin {
    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;getOpposite()Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing flip(EnumFacing facing, World worldIn, BlockPos pos, EnumFacing facingArg, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (BlockRotator.flippinEligibility(placer)) return facing; // flipped twice
        return facing.getOpposite();
    }
}
