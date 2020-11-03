package carpet.mixin.repeaterPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRedstoneRepeater.class)
public class BlockRedstoneRepeaterMixin extends BlockRedstoneDiodeMixin {
    @Shadow @Final public static PropertyInteger DELAY;

    @Override
    protected int getDelay(IBlockState state, World world, BlockPos pos) {
        int delay = 2;
        // Added repeater with adjustable delay on terracota CARPET-XCOM
        if (CarpetSettings.repeaterPoweredTerracotta) {
            IBlockState stateBelow = world.getBlockState(pos.down());
            Block blockBelow = stateBelow.getBlock();
            if (blockBelow == Blocks.STAINED_HARDENED_CLAY) {
                delay = blockBelow.getMetaFromState(stateBelow);
                if (delay == 0) delay = 100;
            }
        }

        return state.getValue(DELAY) * delay;
    }
}
