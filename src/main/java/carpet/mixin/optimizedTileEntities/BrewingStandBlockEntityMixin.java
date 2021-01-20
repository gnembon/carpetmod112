package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.BlockEntityOptimizer;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin implements BlockEntityOptimizer.LazyBlockEntity {
    @Shadow private int brewTime;
    private boolean sleeping;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sleep(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void checkSleep(CallbackInfo ci) {
        if (brewTime == 0) sleeping = true;
    }

    @Override
    public void wakeUp() {
        sleeping = false;
    }
}
