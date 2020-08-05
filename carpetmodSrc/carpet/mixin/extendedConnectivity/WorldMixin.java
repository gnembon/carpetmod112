package carpet.mixin.extendedConnectivity;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos);
    @Shadow public abstract void observedNeighborChanged(BlockPos pos, Block changedBlock, BlockPos changedBlockPos);

    @Inject(method = "notifyNeighborsOfStateChange", at = @At("HEAD"))
    private void extendedConnectivityNotify(BlockPos pos, Block blockType, boolean updateObservers, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            this.neighborChanged(posd.west(), blockType, pos);
            this.neighborChanged(posd.east(), blockType, pos);
            this.neighborChanged(posd.down(), blockType, pos);
            //this.neighborChanged(pos.up(), blockType);
            this.neighborChanged(posd.north(), blockType, pos);
            this.neighborChanged(posd.south(), blockType, pos);
            if (updateObservers) {
                this.observedNeighborChanged(posd.west(), blockType, posd);
                this.observedNeighborChanged(posd.east(), blockType, posd);
                this.observedNeighborChanged(posd.down(), blockType, posd);
                //this.observedNeighborChanged(pos.up(), blockType, pos);
                this.observedNeighborChanged(posd.north(), blockType, posd);
                this.observedNeighborChanged(posd.south(), blockType, posd);
            }
        }
    }

    @Inject(method = "notifyNeighborsOfStateExcept", at = @At("HEAD"))
    private void extendedConnectivityExcept(BlockPos pos, Block blockType, EnumFacing skipSide, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            if (skipSide != EnumFacing.WEST) this.neighborChanged(posd.west(), blockType, posd);
            if (skipSide != EnumFacing.EAST) this.neighborChanged(posd.east(), blockType, posd);
            if (skipSide != EnumFacing.DOWN) this.neighborChanged(posd.down(), blockType, posd);
            //if (skipSide != EnumFacing.UP) this.neighborChanged(pos.up(), blockType, posd);
            if (skipSide != EnumFacing.NORTH) this.neighborChanged(posd.north(), blockType, posd);
            if (skipSide != EnumFacing.SOUTH) this.neighborChanged(posd.south(), blockType, posd);
        }
    }
}
