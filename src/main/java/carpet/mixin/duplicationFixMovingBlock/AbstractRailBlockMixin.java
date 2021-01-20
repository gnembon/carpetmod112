package carpet.mixin.duplicationFixMovingBlock;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRailBlock.class)
public class AbstractRailBlockMixin {
    @Inject(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRailBlock;method_26417(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V"), cancellable = true)
    private void fixDupe(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.duplicationFixMovingRail && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
