package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.world.gen.feature.EndCityFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCityFeature.Start.class)
public class EndCityFeatureStartMixin {
    @Inject(method = "method_27889", at = @At("HEAD"), cancellable = true)
    private void alwaysSizeable(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities) cir.setReturnValue(true);
    }
}
