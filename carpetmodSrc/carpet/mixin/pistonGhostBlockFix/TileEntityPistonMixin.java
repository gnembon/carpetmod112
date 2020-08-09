package carpet.mixin.pistonGhostBlockFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedTileEntityPistonGhostBlockFix;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin extends TileEntity implements ExtendedTileEntityPistonGhostBlockFix {
    @Shadow private float lastProgress;
    private long lastTicked;

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        this.lastTicked = this.world.getTotalWorldTime();
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private void onSetBlockState(CallbackInfo ci) {
        if (CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.serverOnly) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(pos.offset(state.getValue(BlockPistonExtension.FACING).getOpposite()), state, state, 0);
        }
    }

    @Override
    public long getLastTicked() {
        return lastTicked;
    }

    @Override
    public float getLastProgress() {
        return lastProgress;
    }
}
