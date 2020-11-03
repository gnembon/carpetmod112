package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.tileentity.TileEntityBrewingStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityBrewingStand.class)
public class TileEntityBrewingStandMixin implements TileEntityOptimizer.ILazyTileEntity {
    @Shadow private int brewTime;
    private boolean sleeping;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void sleep(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void checkSleep(CallbackInfo ci) {
        if (brewTime == 0) sleeping = true;
    }

    @Override
    public void wakeUp() {
        sleeping = false;
    }
}
