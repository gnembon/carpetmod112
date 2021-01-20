package carpet.mixin.redstoneDustAlgorithm;

import carpet.CarpetSettings;
import carpet.helpers.RedstoneWireTurbo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.List;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
    @Shadow @Final public static IntProperty field_24710;
    @Shadow protected abstract BlockState method_26769(World worldIn, BlockPos pos, BlockState state);
    @Shadow protected abstract int method_26761(World worldIn, BlockPos pos, int strength);

    private final RedstoneWireTurbo turbo = new RedstoneWireTurbo((RedstoneWireBlock) (Object) this);
    private static final ThreadLocal<BlockPos> source = new ThreadLocal<>();

    @Inject(method = "method_26769", at = @At("HEAD"), cancellable = true)
    private void updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (CarpetSettings.redstoneDustAlgorithm == CarpetSettings.RedstoneDustAlgorithm.fast) {
            cir.setReturnValue(turbo.updateSurroundingRedstone(worldIn, pos, state, source.get()));
        }
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @Inject(method = "method_26762", at = @At(value = "FIELD", target = "Lnet/minecraft/block/RedstoneWireBlock;wiresGivePower:Z", ordinal = 1, shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void calculateCurrentChanges(World world, BlockPos pos1, BlockPos pos2, BlockState state, CallbackInfoReturnable<BlockState> cir, BlockState iblockstate, int i, int power, int fromNeighbors) {
        if (CarpetSettings.redstoneDustAlgorithm != CarpetSettings.RedstoneDustAlgorithm.fast) return;
        int maxCurrentStrength = 0;
        if (fromNeighbors < 15) {
            for (Direction enumfacing : Direction.Type.HORIZONTAL) {
                BlockPos blockpos = pos1.offset(enumfacing);
                boolean isNeighbor = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (isNeighbor) {
                    maxCurrentStrength = this.method_26761(world, blockpos, maxCurrentStrength);
                }

                if (world.getBlockState(blockpos).isSolidBlock() && !world.getBlockState(pos1.up()).isSolidBlock()) {
                    if (isNeighbor && pos1.getY() >= pos2.getY()) {
                        maxCurrentStrength = this.method_26761(world, blockpos.up(), maxCurrentStrength);
                    }
                } else if (!world.getBlockState(blockpos).isSolidBlock() && isNeighbor && pos1.getY() <= pos2.getY()) {
                    maxCurrentStrength = this.method_26761(world, blockpos.down(), maxCurrentStrength);
                }
            }
        }
        power = maxCurrentStrength - 1;
        if (fromNeighbors > power) power = fromNeighbors;
        if (i != power) {
            state = state.with(field_24710, power);

            if (world.getBlockState(pos1) == iblockstate) {
                world.setBlockState(pos1, state, 2);
            }
        }
        cir.setReturnValue(state);
    }

    @Inject(method = "method_26769", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void shuffleUpdateOrder(World worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir, List<BlockPos> list) {
        if (CarpetSettings.redstoneDustAlgorithm == CarpetSettings.RedstoneDustAlgorithm.random) {
            Collections.shuffle(list);
        }
    }

    @Redirect(method = "onBlockRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;method_26769(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"))
    private BlockState updateOnBreak(RedstoneWireBlock wire, World worldIn, BlockPos pos, BlockState state) {
        source.set(null);
        return method_26769(worldIn, pos, state);
    }

    @Redirect(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;method_26769(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"))
    private BlockState updateOnNeighborChanged(RedstoneWireBlock wire, World worldIn, BlockPos pos, BlockState state, BlockState state2, World worldIn2, BlockPos pos2, Block blockIn, BlockPos fromPos) {
        source.set(fromPos);
        return method_26769(worldIn, pos, state);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;method_26769(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"))
    private BlockState updateOnBlockAdded(RedstoneWireBlock wire, World worldIn, BlockPos pos, BlockState state) {
        source.set(null);
        return method_26769(worldIn, pos, state);
    }
}
