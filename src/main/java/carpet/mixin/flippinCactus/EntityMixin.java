package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "getHorizontalFacing", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;fromHorizontal(I)Lnet/minecraft/util/math/Direction;"))
    private Direction byHorizontalIndex(int index) {
        Direction facing = Direction.fromHorizontal(index);
        if (BlockRotator.flippinEligibility((Entity) (Object) this)) return facing.getOpposite();
        return facing;
    }
}
