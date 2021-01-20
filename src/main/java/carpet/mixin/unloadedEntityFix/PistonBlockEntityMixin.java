package carpet.mixin.unloadedEntityFix;

import carpet.CarpetSettings;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @Redirect(method = {"method_27160", "push"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;method_34411(Lnet/minecraft/entity/MovementType;DDD)V"))
    private void moveAndUpdate(Entity entity, MovementType type, double x, double y, double z) {
        entity.method_34411(type, x, y, z);
        if (CarpetSettings.unloadedEntityFix) {
            // Add entity to the correct chunk after moving
            entity.world.method_26050(entity, false);
        }
    }
}
