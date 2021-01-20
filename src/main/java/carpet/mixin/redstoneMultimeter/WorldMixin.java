package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import narcolepticfrog.rsmm.events.StateChangeEventDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow @Final public boolean isClient;

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "updateNeighbor", at = @At("RETURN"))
    private void onNeighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.redstoneMultimeter && !isClient) StateChangeEventDispatcher.dispatchEvent((World) (Object) this, pos);
    }

    @Inject(method = "onBlockChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/ObserverBlock;method_26711(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.AFTER))
    private void onObservedNeighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.redstoneMultimeter) StateChangeEventDispatcher.dispatchEvent((World) (Object) this, pos);
    }

    @Inject(method = "updateHorizontalAdjacent", at = @At("RETURN"))
    private void onComparatorUpdate(BlockPos pos, Block blockIn, CallbackInfo ci) {
        if (CarpetSettings.redstoneMultimeter) StateChangeEventDispatcher.dispatchEvent((World) (Object) this, pos);
    }
}
