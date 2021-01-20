package carpet.mixin.duplicationFixPortalEntitys;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "canDropLootAndXp", at = @At("HEAD"), cancellable = true)
    private void dupeFix(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.duplicationFixPortalEntitys && removed) cir.setReturnValue(false);
    }
}
