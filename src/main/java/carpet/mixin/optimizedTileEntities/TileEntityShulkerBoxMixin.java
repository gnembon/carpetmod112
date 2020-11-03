package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.tileentity.TileEntityShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityShulkerBox.class)
public abstract class TileEntityShulkerBoxMixin extends TileEntityLockableLoot implements TileEntityOptimizer.ILazyTileEntity {
    @Shadow private TileEntityShulkerBox.AnimationStatus animationStatus;
    // CARPET-optimizedTileEntities: Whether the tile entity is asleep or not.
    // False by default so tile entities wake up upon chunk loading
    private boolean sleeping = false;

    @Override
    public void wakeUp(){
        this.sleeping = false;
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void optimize(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "updateAnimation", at = @At("RETURN"))
    private void onUpdateAnimation(CallbackInfo ci) {
        TileEntityShulkerBox.AnimationStatus status = animationStatus;
        sleeping = status == TileEntityShulkerBox.AnimationStatus.OPENED || status == TileEntityShulkerBox.AnimationStatus.CLOSED;
    }

    @Inject(method = "receiveClientEvent", at = @At("HEAD"))
    private void onClientEvent(int id, int type, CallbackInfoReturnable<Boolean> cir) {
        sleeping = false;
    }
}
