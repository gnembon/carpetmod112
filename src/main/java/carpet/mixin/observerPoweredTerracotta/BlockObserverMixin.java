package carpet.mixin.observerPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockObserver.class)
public class BlockObserverMixin extends BlockDirectional {
    protected BlockObserverMixin(Material materialIn) {
        super(materialIn);
    }

    @ModifyConstant(method = "startSignal", constant = @Constant(intValue = 2))
    private int adjustDelay(int delay, IBlockState state, World world, BlockPos pos) {
        if (CarpetSettings.observerPoweredTerracotta){
            EnumFacing enumfacing = state.getValue(FACING);
            BlockPos blockpos = pos.offset(enumfacing.getOpposite());
            IBlockState iblockstate = world.getBlockState(blockpos);
            Block block = iblockstate.getBlock();
            if (block == Blocks.STAINED_HARDENED_CLAY){
                delay = block.getMetaFromState(iblockstate);
                if (delay == 0) delay = 100;
            }
        }
        return delay;
    }
}
