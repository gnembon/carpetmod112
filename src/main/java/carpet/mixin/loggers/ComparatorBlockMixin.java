package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.InstantComparators;
import carpet.utils.extensions.ExtendedComparatorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
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

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin {
    @Shadow protected abstract int calculateOutputSignal(World worldIn, BlockPos pos, BlockState state);

    @Shadow protected abstract boolean method_26545(BlockState state);

    @Shadow protected abstract boolean hasPower(World worldIn, BlockPos pos, BlockState state);

    @Inject(method = "scheduledTick", at = @At("RETURN"))
    private void logOnUpdateTick(World worldIn, BlockPos pos, BlockState state, Random rand, CallbackInfo ci) {
        if (LoggerRegistry.__instantComparators) {
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof ComparatorBlockEntity) {
                ComparatorBlockEntity comparator = (ComparatorBlockEntity) te;
                int index = (int) Math.floorMod(worldIn.getTime(), 3);
                // output signal 0 is generally considered to just be a too fast pulse for a comparator, rather
                // than an instant comparator
                ExtendedComparatorBlockEntity ext = (ExtendedComparatorBlockEntity) comparator;
                if (comparator.getOutputSignal() != ext.getScheduledOutputSignal()[index] && comparator.getOutputSignal() != 0) {
                    InstantComparators.onInstantComparator(worldIn, pos, ext.getBuggy()[index]);
                }
            }
        }
    }

    @Inject(method = "method_26557", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logOnPowerChange(World world, BlockPos pos, BlockState state, CallbackInfo ci, int computedOutput, BlockEntity blockEntity) {
        int currentOutput = blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockEntity).getOutputSignal() : 0;
        if (LoggerRegistry.__instantComparators && (currentOutput != computedOutput || this.method_26545(state) != this.hasPower(world, pos, state))) {
            if (blockEntity instanceof ComparatorBlockEntity) {
                ComparatorBlockEntity comparator = (ComparatorBlockEntity) blockEntity;
                int index = (int) Math.floorMod(world.getTime() + 2, 3);
                ExtendedComparatorBlockEntity ext = (ExtendedComparatorBlockEntity) comparator;
                ext.getScheduledOutputSignal()[index] = computedOutput;
                ext.getBuggy()[index] = computedOutput == currentOutput;
            } else {
                InstantComparators.onNoTileEntity(world, pos);
            }
        }
    }

    @Redirect(method = "method_26557", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26012(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Z"))
    private boolean logOnTickPending(World world, BlockPos pos, Block block, World world2, BlockPos pos2, BlockState state) {
        if (!world.method_26012(pos, block)) return false;
        if (LoggerRegistry.__instantComparators) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ComparatorBlockEntity) {
                ComparatorBlockEntity comparator = (ComparatorBlockEntity) te;
                int index = (int) Math.floorMod(world.getTime() + 2, 3);
                ExtendedComparatorBlockEntity ext = (ExtendedComparatorBlockEntity) comparator;
                ext.getScheduledOutputSignal()[index] = calculateOutputSignal(world, pos, state);
            }
        }
        return true;
    }
}
