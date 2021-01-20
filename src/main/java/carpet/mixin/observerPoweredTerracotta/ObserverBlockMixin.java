package carpet.mixin.observerPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ObserverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin extends FacingBlock {
    protected ObserverBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @ModifyConstant(method = "method_26712", constant = @Constant(intValue = 2))
    private int adjustDelay(int delay, BlockState state, World world, BlockPos pos) {
        if (CarpetSettings.observerPoweredTerracotta){
            Direction enumfacing = state.get(field_24311);
            BlockPos blockpos = pos.offset(enumfacing.getOpposite());
            BlockState iblockstate = world.getBlockState(blockpos);
            Block block = iblockstate.getBlock();
            if (block == Blocks.STAINED_TERRACOTTA){
                delay = block.getMeta(iblockstate);
                if (delay == 0) delay = 100;
            }
        }
        return delay;
    }
}
