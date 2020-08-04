package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.world.gen.structure.MapGenEndCity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapGenEndCity.Start.class)
public class MapGenEndCityStartMixin {
    @Inject(method = "isSizeableStructure", at = @At("HEAD"), cancellable = true)
    private void alwaysSizeable(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities) cir.setReturnValue(true);
    }
}
