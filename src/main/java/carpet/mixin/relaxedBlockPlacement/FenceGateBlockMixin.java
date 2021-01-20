package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockMixin extends HorizontalFacingBlock {
    protected FenceGateBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "canReplace", at = @At("HEAD"), cancellable = true)
    private void allowMidAir(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.relaxedBlockPlacement) {
            cir.setReturnValue(super.canReplace(world, pos));
        }
    }
}
