package narcolepticfrog.rsmm.meterable;

import carpet.mixin.accessors.PistonBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class IsPoweredHelper {

    public static boolean isPowered(BlockState state, BlockView source, BlockPos pos) {
        if (!(source instanceof World)) {
            return false;
        }
        Block block = state.getBlock();
        World world = (World)source;

        if (block instanceof AirBlock) {
            return false;
        } else if (block instanceof AbstractButtonBlock) {
            return state.get(AbstractButtonBlock.field_24240);
        } else if (block instanceof DispenserBlock) {
            return world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        } else if (block instanceof LeverBlock) {
            return state.get(LeverBlock.field_24533);
        } else if (block instanceof ObserverBlock) {
            return state.get(ObserverBlock.field_24607);
        } else if (block instanceof PistonBlock) {
            return ((PistonBlockAccessor) block).invokeShouldExtend(world, pos, state.get(FacingBlock.FACING));
        } else if (block instanceof WeightedPressurePlateBlock) {
            return state.get(WeightedPressurePlateBlock.field_25004) > 0;
        } else if (block instanceof PressurePlateBlock) {
            return state.get(PressurePlateBlock.field_24645);
        } else if (block instanceof DetectorRailBlock) {
            return state.get(DetectorRailBlock.field_24305);
        } else if (block instanceof PoweredRailBlock) {
            return state.get(PoweredRailBlock.POWERED);
        } else if (block instanceof ComparatorBlock) {
            if (state.get(ComparatorBlock.field_24288) == ComparatorBlock.ComparatorMode.COMPARE) {
                return state.get(ComparatorBlock.field_24287);
            } else {
                return ((ComparatorBlockEntity) source.getBlockEntity(pos)).getOutputSignal() > 0;
            }
        } else if (block instanceof RepeaterBlock) {
            return block == Blocks.POWERED_REPEATER;
        } else if (block instanceof RedstoneTorchBlock) {
            return block == Blocks.REDSTONE_TORCH;
        } else if (block instanceof RedstoneWireBlock) {
            return state.get(RedstoneWireBlock.field_24710) > 0;
        } else if (block instanceof TripwireHookBlock) {
            return state.get(TripwireHookBlock.field_24954);
        } else {
            return world.isReceivingRedstonePower(pos);
        }
    }

}
