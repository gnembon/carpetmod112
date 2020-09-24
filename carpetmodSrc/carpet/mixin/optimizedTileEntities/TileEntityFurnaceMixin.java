package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityFurnace.class)
public abstract class TileEntityFurnaceMixin implements TileEntityOptimizer.ILazyTileEntity {
    @Shadow public abstract boolean isBurning();

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

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityFurnace;markDirty()V"))
    private void checkSleep(CallbackInfo ci) {
        if (!isBurning()) sleeping = true;
    }
}
