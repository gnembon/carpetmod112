package carpet.mixin.potions;

import carpet.utils.extensions.ExtendedPotionEffect;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityPotion.class)
public class EntityPotionMixin {
    @Inject(method = "applySplash", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;addPotionEffect(Lnet/minecraft/potion/PotionEffect;)V"))
    private void preAddPotionEffect(RayTraceResult p_190543_1_, List<PotionEffect> p_190543_2_, CallbackInfo ci) {
        ExtendedPotionEffect.ItemPotionHolder.itemPotion = true;
    }

    @Inject(method = "applySplash", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;addPotionEffect(Lnet/minecraft/potion/PotionEffect;)V", shift = At.Shift.AFTER))
    private void postAddPotionEffect(RayTraceResult p_190543_1_, List<PotionEffect> p_190543_2_, CallbackInfo ci) {
        ExtendedPotionEffect.ItemPotionHolder.itemPotion = false;
    }
}
