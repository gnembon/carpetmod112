package carpet.mixin.reloadEntitySpeedlossFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    // Fix to entitys losing speed when reloaded CARPET-XCOM
    @Redirect(method = "fromTag", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", remap = false))
    private double abs(double a) {
        return CarpetSettings.reloadEntitySpeedlossFix ? 0 : Math.abs(a);
    }
}
