package carpet.mixin.railRotationFix;

import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
    BlockRail.class,
    BlockRailDetector.class,
    BlockRailPowered.class
})
public class RailBlocksMixin {
    @Inject(method = "withRotation", at = @At("HEAD"), cancellable = true)
    private void fixControlFlow(IBlockState state, Rotation rot, CallbackInfoReturnable<IBlockState> cir) {
        if (rot != Rotation.CLOCKWISE_180) return;
        BlockRailBase.EnumRailDirection shape = state.getValue(BlockRail.SHAPE);
        if (shape == BlockRailBase.EnumRailDirection.NORTH_SOUTH || shape == BlockRailBase.EnumRailDirection.EAST_WEST) {
            // these don't change the state but the missing cases in vanilla fall through to COUNTERCLOCKWISE_90
            // leading to incorrect results
            cir.setReturnValue(state);
        }
    }
}
