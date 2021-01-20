package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.BlockEntityOptimizer;
import net.minecraft.block.entity.FurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin implements BlockEntityOptimizer.LazyBlockEntity {
    @Shadow public abstract boolean method_26991();

    // CARPET-optimizedTileEntities: Whether the tile entity is asleep or not.
    // False by default so tile entities wake up upon chunk loading
    private boolean sleeping = false;

    @Override
    public void wakeUp(){
        this.sleeping = false;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void optimize(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FurnaceBlockEntity;markDirty()V"))
    private void checkSleep(CallbackInfo ci) {
        if (!method_26991()) sleeping = true;
    }
}
