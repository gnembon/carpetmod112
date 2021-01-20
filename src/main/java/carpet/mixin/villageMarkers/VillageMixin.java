package carpet.mixin.villageMarkers;

import carpet.utils.extensions.ExtendedVillageCollection;
import net.minecraft.class_2792;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2792.class)
public class VillageMixin {
    @Shadow private World field_33643;

    @Inject(method = "method_35107", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", remap = false))
    private void updateOldDoors(CallbackInfo ci) {
        ((ExtendedVillageCollection) field_33643.method_26061()).markVillageMarkersDirty();
    }
}
