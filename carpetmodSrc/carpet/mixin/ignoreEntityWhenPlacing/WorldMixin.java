package carpet.mixin.ignoreEntityWhenPlacing;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z"))
    private boolean ignoreEntityWhenPlacing(World world, AxisAlignedBB box, Entity entity) {
        return CarpetSettings.ignoreEntityWhenPlacing || world.checkNoEntityCollision(box, entity);
    }
}
