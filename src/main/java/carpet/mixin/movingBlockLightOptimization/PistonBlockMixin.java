package carpet.mixin.movingBlockLightOptimization;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin extends FacingBlock {
    protected PistonBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 1))
    private boolean setBlockState1(World world, BlockPos pos, BlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 2))
    private boolean setBlockState2(World world, BlockPos pos, BlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    // Added the properties of opacity and light to the moving block as to minimize light updates. CARPET-XCOM
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void movingBlockLightOptimization(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                              PistonHandler helper, List<BlockPos> positions, List<BlockState> states, List<BlockPos> list2, int k, BlockState[] aiblockstate, Direction enumfacing, int index, BlockPos currentPos, BlockState currentState) {
        if (!CarpetSettings.movingBlockLightOptimization) return;
        BlockPos posOld = positions.get(index);
        boolean remove = true;
        for (int backwardCheck = index - 1; backwardCheck >= 0; --backwardCheck){
            BlockPos blockposCheck = positions.get(backwardCheck);
            if(blockposCheck.offset(enumfacing).equals(posOld)){
                remove = false;
                break;
            }
        }
        BlockState movingBlock = Blocks.PISTON_EXTENSION.getDefaultState().with(FACING, direction)
                .with(PistonHelper.OPACITY, Math.min(currentState.getOpacity(), 15))
                .with(PistonHelper.LIGHT, currentState.getLuminance());
        worldIn.setBlockState(currentPos, movingBlock, 20);
        if (remove){
            worldIn.setBlockState(posOld, Blocks.AIR.getDefaultState(), 2);
        }
        worldIn.method_26099(currentPos, movingBlock.getBlock());
    }
}
