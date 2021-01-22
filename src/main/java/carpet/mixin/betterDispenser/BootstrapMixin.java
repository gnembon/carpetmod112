package carpet.mixin.betterDispenser;

import carpet.helpers.BetterDispenser;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "setupDispenserBehavior", at = @At("RETURN"))
    private static void addBetterDispenserBehaviors(CallbackInfo ci) {
        // Carpet Dispenser addons XCOM-CARPET
        BetterDispenser.dispenserAddons();
    }
}
