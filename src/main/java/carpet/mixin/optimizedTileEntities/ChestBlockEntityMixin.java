package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.BlockEntityOptimizer;
import net.minecraft.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin implements BlockEntityOptimizer.LazyBlockEntity {
    @Shadow public int viewerCount;
    private boolean sleeping;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sleep(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/ChestBlockEntity;field_25099:F"))
    private void checkSleepPlayers(CallbackInfo ci) {
        if (viewerCount == 0) sleeping = true;
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/ChestBlockEntity;field_25098:F", ordinal = 4))
    private void wakeUpFromLid(CallbackInfo ci) {
        // This is the closing animation.
        // It is possible here that numPlayersUsing is 0, so make sure you don't sleep
        sleeping = false;
    }

    @Inject(method = "onBlockAction", at = @At("HEAD"))
    private void wakeUpFromClientEvent(int id, int type, CallbackInfoReturnable<Boolean> cir) {
        sleeping = false;
    }

    @Override
    public void wakeUp() {
        sleeping = false;
    }
}
