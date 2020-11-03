package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.PistonFixes;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        if (CarpetSettings.pistonClippingFix > 0) PistonFixes.synchronizeClient();
    }
}
