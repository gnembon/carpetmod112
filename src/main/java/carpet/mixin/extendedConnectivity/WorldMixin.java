package carpet.mixin.extendedConnectivity;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract void updateNeighbor(BlockPos pos, Block blockIn, BlockPos fromPos);
    @Shadow public abstract void onBlockChanged(BlockPos pos, Block changedBlock, BlockPos changedBlockPos);

    @Inject(method = "updateNeighborsAlways", at = @At("HEAD"))
    private void extendedConnectivityNotify(BlockPos pos, Block blockType, boolean updateObservers, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            this.updateNeighbor(posd.west(), blockType, pos);
            this.updateNeighbor(posd.east(), blockType, pos);
            this.updateNeighbor(posd.down(), blockType, pos);
            //this.updateNeighbor(pos.up(), blockType);
            this.updateNeighbor(posd.north(), blockType, pos);
            this.updateNeighbor(posd.south(), blockType, pos);
            if (updateObservers) {
                this.onBlockChanged(posd.west(), blockType, posd);
                this.onBlockChanged(posd.east(), blockType, posd);
                this.onBlockChanged(posd.down(), blockType, posd);
                //this.onBlockChanged(pos.up(), blockType, pos);
                this.onBlockChanged(posd.north(), blockType, posd);
                this.onBlockChanged(posd.south(), blockType, posd);
            }
        }
    }

    @Inject(method = "updateNeighborsExcept", at = @At("HEAD"))
    private void extendedConnectivityExcept(BlockPos pos, Block blockType, Direction skipSide, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            if (skipSide != Direction.WEST) this.updateNeighbor(posd.west(), blockType, posd);
            if (skipSide != Direction.EAST) this.updateNeighbor(posd.east(), blockType, posd);
            if (skipSide != Direction.DOWN) this.updateNeighbor(posd.down(), blockType, posd);
            //if (skipSide != Direction.UP) this.updateNeighbor(pos.up(), blockType, posd);
            if (skipSide != Direction.NORTH) this.updateNeighbor(posd.north(), blockType, posd);
            if (skipSide != Direction.SOUTH) this.updateNeighbor(posd.south(), blockType, posd);
        }
    }
}
