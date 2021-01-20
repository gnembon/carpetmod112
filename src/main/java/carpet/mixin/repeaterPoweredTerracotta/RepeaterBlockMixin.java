package carpet.mixin.repeaterPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RepeaterBlock.class)
public class RepeaterBlockMixin extends AbstractRedstoneGateBlockMixin {
    @Shadow @Final public static IntProperty field_24733;

    @Override
    protected int getDelay(BlockState state, World world, BlockPos pos) {
        int delay = 2;
        // Added repeater with adjustable delay on terracota CARPET-XCOM
        if (CarpetSettings.repeaterPoweredTerracotta) {
            BlockState stateBelow = world.getBlockState(pos.method_31898());
            Block blockBelow = stateBelow.getBlock();
            if (blockBelow == Blocks.STAINED_TERRACOTTA) {
                delay = blockBelow.getMeta(stateBelow);
                if (delay == 0) delay = 100;
            }
        }

        return state.get(field_24733) * delay;
    }
}
