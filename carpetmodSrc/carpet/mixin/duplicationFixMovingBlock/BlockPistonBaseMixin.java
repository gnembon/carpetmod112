package carpet.mixin.duplicationFixMovingBlock;

import carpet.helpers.PistonHelper;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin {
    @Inject(method = "doMove", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/state/BlockPistonStructureHelper;getBlocksToMove()Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void dupeFixStart(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir, BlockPistonStructureHelper helper, List<BlockPos> moving) {
        PistonHelper.registerPushed(moving);
    }

    @Inject(method = "doMove", at = @At(value = "RETURN", ordinal = 1))
    private void dupeFixEnd(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        PistonHelper.finishPush();
    }
}
