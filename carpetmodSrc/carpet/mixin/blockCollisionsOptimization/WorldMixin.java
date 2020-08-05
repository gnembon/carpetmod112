package carpet.mixin.blockCollisionsOptimization;

import carpet.CarpetSettings;
import carpet.helpers.CollisionBoxesOptimizations;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z", at = @At("HEAD"), cancellable = true)
    private void blockCollisionsOptimization(Entity entityIn, AxisAlignedBB aabb, boolean flag, List<AxisAlignedBB> outList, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.blockCollisionsOptimization) {
            cir.setReturnValue(CollisionBoxesOptimizations.optimizedGetCollisionBoxes((World) (Object) this, entityIn, aabb, flag, outList));
        }
    }
}
