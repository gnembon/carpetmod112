package narcolepticfrog.rsmm.meterable;

import carpet.helpers.RedstoneWireTurbo;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class IsPoweredHelper {

    public static boolean isPowered(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (!(source instanceof World)) {
            return false;
        }
        Block block = state.getBlock();
        World world = (World)source;

        if (block instanceof BlockAir) {
            return false;
        } else if (block instanceof BlockButton) {
            return state.getValue(BlockButton.POWERED);
        } else if (block instanceof BlockDispenser) {
            return world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
        } else if (block instanceof BlockLever) {
            return state.getValue(BlockLever.POWERED);
        } else if (block instanceof BlockObserver) {
            return state.getValue(BlockObserver.POWERED);
        } else if (block instanceof BlockPistonBase) {
            return ((BlockPistonBase) (block)).shouldBeExtended(world, pos, state.getValue(BlockDirectional.FACING));
        } else if (block instanceof BlockPressurePlateWeighted) {
            return state.getValue(BlockPressurePlateWeighted.POWER) > 0;
        } else if (block instanceof BlockPressurePlate) {
            return state.getValue(BlockPressurePlate.POWERED);
        } else if (block instanceof BlockRailDetector) {
            return state.getValue(BlockRailDetector.POWERED);
        } else if (block instanceof BlockRailPowered) {
            return state.getValue(BlockRailPowered.POWERED);
        } else if (block instanceof BlockRedstoneComparator) {
            if (state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.COMPARE) {
                return state.getValue(BlockRedstoneComparator.POWERED);
            } else {
                return ((TileEntityComparator) source.getTileEntity(pos)).getOutputSignal() > 0;
            }
        } else if (block instanceof BlockRedstoneRepeater) {
            return block == Blocks.POWERED_REPEATER;
        } else if (block instanceof BlockRedstoneTorch) {
            return block == Blocks.REDSTONE_TORCH;
        } else if (block instanceof BlockRedstoneWire) {
            return state.getValue(BlockRedstoneWire.POWER) > 0;
        } else if (block instanceof BlockTripWireHook) {
            return state.getValue(BlockTripWireHook.POWERED);
        } else {
            return world.isBlockPowered(pos);
        }
    }

}
