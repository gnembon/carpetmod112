package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.PistonFixes;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        if (CarpetSettings.pistonClippingFix > 0) PistonFixes.synchronizeClient();
    }
}
