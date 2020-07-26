package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityChest.class)
public class TileEntityChestMixin implements TileEntityOptimizer.ILazyTileEntity {
    @Shadow public int numPlayersUsing;
    private boolean sleeping;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void sleep(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityChest;prevLidAngle:F"))
    private void checkSleepPlayers(CallbackInfo ci) {
        if (numPlayersUsing == 0) sleeping = true;
    }

    @Inject(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityChest;lidAngle:F", ordinal = 4))
    private void wakeUpFromLid(CallbackInfo ci) {
        // This is the closing animation.
        // It is possible here that numPlayersUsing is 0, so make sure you don't sleep
        sleeping = false;
    }

    @Inject(method = "receiveClientEvent", at = @At("HEAD"))
    private void wakeUpFromClientEvent(int id, int type, CallbackInfoReturnable<Boolean> cir) {
        sleeping = false;
    }

    @Override
    public void wakeUp() {
        sleeping = false;
    }
}
