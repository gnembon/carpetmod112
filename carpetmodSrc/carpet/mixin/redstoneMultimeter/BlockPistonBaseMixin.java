package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import narcolepticfrog.rsmm.events.PistonPushEventDispatcher;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
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
    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onPush(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                        BlockPistonStructureHelper helper, List<BlockPos> positions, List<IBlockState> states, List<BlockPos> list2, int k, IBlockState[] aiblockstate, EnumFacing movementDirection, int index, BlockPos currentPos) {
        if (CarpetSettings.redstoneMultimeter) {
            PistonPushEventDispatcher.dispatchEvent(worldIn, currentPos, movementDirection);
        }
    }
}
