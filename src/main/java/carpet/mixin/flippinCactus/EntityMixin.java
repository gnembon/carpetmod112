package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "getHorizontalFacing", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;byHorizontalIndex(I)Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing byHorizontalIndex(int index) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(index);
        if (BlockRotator.flippinEligibility((Entity) (Object) this)) return facing.getOpposite();
        return facing;
    }
}
