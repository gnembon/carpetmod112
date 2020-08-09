package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.InstantComparators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(BlockRedstoneComparator.class)
public abstract class BlockRedstoneComparatorMixin {
    @Shadow protected abstract int calculateOutput(World worldIn, BlockPos pos, IBlockState state);

    @Inject(method = "updateTick", at = @At("RETURN"))
    private void logOnUpdateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (LoggerRegistry.__instantComparators) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) te;
                int index = (int) Math.floorMod(worldIn.getTotalWorldTime(), 3);
                // output signal 0 is generally considered to just be a too fast pulse for a comparator, rather
                // than an instant comparator
                if (comparator.getOutputSignal() != comparator.scheduledOutputSignal[index] && comparator.getOutputSignal() != 0) {
                    InstantComparators.onInstantComparator(worldIn, pos, comparator.buggy[index]);
                }
            }
        }
    }

    @Inject(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneComparator;isFacingTowardsRepeater(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logOnPowerChange(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci, int computedOutput, TileEntity tileentity, int currentOutput) {
        if (LoggerRegistry.__instantComparators) {
            if (tileentity instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) tileentity;
                int index = (int) Math.floorMod(worldIn.getTotalWorldTime() + 2, 3);
                comparator.scheduledOutputSignal[index] = computedOutput;
                comparator.buggy[index] = computedOutput == currentOutput;
            } else {
                InstantComparators.onNoTileEntity(worldIn, pos);
            }
        }
    }

    @Redirect(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockTickPending(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Z"))
    private boolean logOnTickPending(World world, BlockPos pos, Block block, World world2, BlockPos pos2, IBlockState state) {
        if (!world.isBlockTickPending(pos, block)) return false;
        if (LoggerRegistry.__instantComparators) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) te;
                int index = (int) Math.floorMod(world.getTotalWorldTime() + 2, 3);
                comparator.scheduledOutputSignal[index] = calculateOutput(world, pos, state);
            }
        }
        return true;
    }
}
