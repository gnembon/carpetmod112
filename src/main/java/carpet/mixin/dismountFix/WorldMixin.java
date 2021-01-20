package carpet.mixin.dismountFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "method_26050", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;stopRiding()V"))
    private void dismountFix(Entity entity) {
        if (CarpetSettings.dismountFix) {
            // copy of the base Entity::dismountRidingEntity
            Entity ridingEntity = entity.getVehicle();
            if (ridingEntity != null) {
                ((EntityAccessor) entity).setVehicle(null);
                ((EntityAccessor) ridingEntity).invokeRemovePassenger(entity);
            }
        } else {
            entity.stopRiding();
        }
    }
}
