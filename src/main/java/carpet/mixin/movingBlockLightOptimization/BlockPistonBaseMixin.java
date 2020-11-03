package carpet.mixin.movingBlockLightOptimization;

import carpet.CarpetSettings;
import carpet.helpers.PistonHelper;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BlockPistonBase.class)
public class BlockPistonBaseMixin extends BlockDirectional {
    protected BlockPistonBaseMixin(Material materialIn) {
        super(materialIn);
    }

    @Redirect(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", ordinal = 1))
    private boolean setBlockState1(World world, BlockPos pos, IBlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    @Redirect(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", ordinal = 2))
    private boolean setBlockState2(World world, BlockPos pos, IBlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    // Added the properties of opacity and light to the moving block as to minimize light updates. CARPET-XCOM
    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void movingBlockLightOptimization(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                              BlockPistonStructureHelper helper, List<BlockPos> positions, List<IBlockState> states, List<BlockPos> list2, int k, IBlockState[] aiblockstate, EnumFacing enumfacing, int index, BlockPos currentPos, IBlockState currentState) {
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
        IBlockState movingBlock = Blocks.PISTON_EXTENSION.getDefaultState().withProperty(FACING, direction)
                .withProperty(PistonHelper.OPACITY, Math.min(currentState.getLightOpacity(), 15))
                .withProperty(PistonHelper.LIGHT, currentState.getLightValue());
        worldIn.setBlockState(currentPos, movingBlock, 20);
        if (remove){
            worldIn.setBlockState(posOld, Blocks.AIR.getDefaultState(), 2);
        }
        worldIn.updateObservingBlocksAt(currentPos, movingBlock.getBlock());
    }
}
