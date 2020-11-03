package carpet.mixin.redstoneDustAlgorithm;

import carpet.CarpetSettings;
import carpet.helpers.RedstoneWireTurbo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

@Mixin(BlockRedstoneWire.class)
public abstract class BlockRedstoneWireMixin {
    @Shadow @Final public static PropertyInteger POWER;
    @Shadow protected abstract IBlockState updateSurroundingRedstone(World worldIn, BlockPos pos, IBlockState state);
    @Shadow protected abstract int getMaxCurrentStrength(World worldIn, BlockPos pos, int strength);

    private final RedstoneWireTurbo turbo = new RedstoneWireTurbo((BlockRedstoneWire) (Object) this);
    private static final ThreadLocal<BlockPos> source = new ThreadLocal<>();

    @Inject(method = "updateSurroundingRedstone", at = @At("HEAD"), cancellable = true)
    private void updateSurroundingRedstone(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        if (CarpetSettings.redstoneDustAlgorithm == CarpetSettings.RedstoneDustAlgorithm.fast) {
            cir.setReturnValue(turbo.updateSurroundingRedstone(worldIn, pos, state, source.get()));
        }
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @Inject(method = "calculateCurrentChanges", at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockRedstoneWire;canProvidePower:Z", ordinal = 1, shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void calculateCurrentChanges(World world, BlockPos pos1, BlockPos pos2, IBlockState state, CallbackInfoReturnable<IBlockState> cir, IBlockState iblockstate, int i, int power, int fromNeighbors) {
        if (CarpetSettings.redstoneDustAlgorithm != CarpetSettings.RedstoneDustAlgorithm.fast) return;
        int maxCurrentStrength = 0;
        if (fromNeighbors < 15) {
            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                BlockPos blockpos = pos1.offset(enumfacing);
                boolean isNeighbor = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (isNeighbor) {
                    maxCurrentStrength = this.getMaxCurrentStrength(world, blockpos, maxCurrentStrength);
                }

                if (world.getBlockState(blockpos).isNormalCube() && !world.getBlockState(pos1.up()).isNormalCube()) {
                    if (isNeighbor && pos1.getY() >= pos2.getY()) {
                        maxCurrentStrength = this.getMaxCurrentStrength(world, blockpos.up(), maxCurrentStrength);
                    }
                } else if (!world.getBlockState(blockpos).isNormalCube() && isNeighbor && pos1.getY() <= pos2.getY()) {
                    maxCurrentStrength = this.getMaxCurrentStrength(world, blockpos.down(), maxCurrentStrength);
                }
            }
        }
        power = maxCurrentStrength - 1;
        if (fromNeighbors > power) power = fromNeighbors;
        if (i != power) {
            state = state.withProperty(POWER, power);

            if (world.getBlockState(pos1) == iblockstate) {
                world.setBlockState(pos1, state, 2);
            }
        }
        cir.setReturnValue(state);
    }

    @Inject(method = "updateSurroundingRedstone", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void shuffleUpdateOrder(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir, List<BlockPos> list) {
        if (CarpetSettings.redstoneDustAlgorithm == CarpetSettings.RedstoneDustAlgorithm.random) {
            Collections.shuffle(list);
        }
    }

    @Redirect(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneWire;updateSurroundingRedstone(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState updateOnBreak(BlockRedstoneWire wire, World worldIn, BlockPos pos, IBlockState state) {
        source.set(null);
        return updateSurroundingRedstone(worldIn, pos, state);
    }

    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneWire;updateSurroundingRedstone(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState updateOnNeighborChanged(BlockRedstoneWire wire, World worldIn, BlockPos pos, IBlockState state, IBlockState state2, World worldIn2, BlockPos pos2, Block blockIn, BlockPos fromPos) {
        source.set(fromPos);
        return updateSurroundingRedstone(worldIn, pos, state);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockRedstoneWire;updateSurroundingRedstone(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState updateOnBlockAdded(BlockRedstoneWire wire, World worldIn, BlockPos pos, IBlockState state) {
        source.set(null);
        return updateSurroundingRedstone(worldIn, pos, state);
    }
}
