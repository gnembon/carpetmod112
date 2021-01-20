package carpet.mixin.repeaterPoweredTerracotta;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractRedstoneGateBlock.class)
public abstract class AbstractRedstoneGateBlockMixin {
    protected int getTickDelay(BlockState state, World world, BlockPos pos) {
        return this.getDelay(state, world, pos);
    }

    protected abstract int getDelay(BlockState state, World world, BlockPos pos);

    @Redirect(method = "method_26557", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRedstoneGateBlock;method_26560(Lnet/minecraft/block/BlockState;)I"))
    private int getDelay(AbstractRedstoneGateBlock diode, BlockState state, World world, BlockPos pos) {
        return getDelay(state, world, pos);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRedstoneGateBlock;method_26549(Lnet/minecraft/block/BlockState;)I"))
    private int getTickDelay(AbstractRedstoneGateBlock diode, BlockState state, World world, BlockPos pos) {
        return getTickDelay(state, world, pos);
    }
}
