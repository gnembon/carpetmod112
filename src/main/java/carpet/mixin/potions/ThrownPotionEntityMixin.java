package carpet.mixin.potions;

import carpet.utils.extensions.ExtendedStatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.BlockHitResult;

@Mixin(PotionEntity.class)
public class ThrownPotionEntityMixin {
    @Inject(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private void preAddPotionEffect(BlockHitResult p_190543_1_, List<StatusEffectInstance> p_190543_2_, CallbackInfo ci) {
        ExtendedStatusEffectInstance.ItemPotionHolder.itemPotion = true;
    }

    @Inject(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V", shift = At.Shift.AFTER))
    private void postAddPotionEffect(BlockHitResult p_190543_1_, List<StatusEffectInstance> p_190543_2_, CallbackInfo ci) {
        ExtendedStatusEffectInstance.ItemPotionHolder.itemPotion = false;
    }
}
