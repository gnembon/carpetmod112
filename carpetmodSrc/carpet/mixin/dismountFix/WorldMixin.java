package carpet.mixin.dismountFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;dismountRidingEntity()V"))
    private void dismountFix(Entity entity) {
        if (CarpetSettings.dismountFix) {
            entity.dismountRidingEntityBase();
        } else {
            entity.dismountRidingEntity();
        }
    }
}
