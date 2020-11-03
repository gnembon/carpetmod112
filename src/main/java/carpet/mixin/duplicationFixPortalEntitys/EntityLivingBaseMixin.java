package carpet.mixin.duplicationFixPortalEntitys;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "canDropLoot", at = @At("HEAD"), cancellable = true)
    private void dupeFix(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.duplicationFixPortalEntitys && isDead) cir.setReturnValue(false);
    }
}
