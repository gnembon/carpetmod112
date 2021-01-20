package carpet.mixin.railRotationFix;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.util.BlockRotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
    RailBlock.class,
    DetectorRailBlock.class,
    PoweredRailBlock.class
})
public class RailBlocksMixin {
    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
    private void fixControlFlow(BlockState state, BlockRotation rot, CallbackInfoReturnable<BlockState> cir) {
        if (rot != BlockRotation.CLOCKWISE_180) return;
        AbstractRailBlock.RailShape shape = state.get(RailBlock.field_24691);
        if (shape == AbstractRailBlock.RailShape.NORTH_SOUTH || shape == AbstractRailBlock.RailShape.EAST_WEST) {
            // these don't change the state but the missing cases in vanilla fall through to COUNTERCLOCKWISE_90
            // leading to incorrect results
            cir.setReturnValue(state);
        }
    }
}
