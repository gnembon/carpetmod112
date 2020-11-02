package carpet.mixin.potions;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPotionEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    @Shadow @Final private Map<Potion, PotionEffect> activePotionsMap;
    @Shadow protected int idleTime;
    @Shadow protected abstract void onFinishedPotionEffect(PotionEffect effect);
    @Shadow protected abstract void onNewPotionEffect(PotionEffect id);
    @Shadow protected abstract void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_);
    @Shadow public abstract void addPotionEffect(PotionEffect potioneffectIn);

    @Inject(method = "addPotionEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;combine(Lnet/minecraft/potion/PotionEffect;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void combinePotions(PotionEffect added, CallbackInfo ci, PotionEffect current) {
        PotionEffect newEffect = ((ExtendedPotionEffect) current).combine(added);
        if (newEffect != current) {
            // carpet
            this.activePotionsMap.put(newEffect.getPotion(), newEffect);
            this.onFinishedPotionEffect(current);
            this.onNewPotionEffect(newEffect);
        } else {
            // vanilla
            this.onChangedPotionEffect(newEffect, true);
        }
        ci.cancel();
    }

    @Inject(method = "onFinishedPotionEffect", at = @At("RETURN"))
    private void onPotionFinish(PotionEffect effect, CallbackInfo ci) {
        if (!CarpetSettings.effectsFix) return;
        PotionEffect previous = ((ExtendedPotionEffect) effect).getPrevious();
        if (previous != null) addPotionEffect(previous);
    }

    @Redirect(method = "attackEntityFrom", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;idleTime:I"))
    private void potionDespawnFix1(EntityLivingBase entity, int idleTime) {
        if (!CarpetSettings.potionsDespawnFix) {
            this.idleTime = idleTime;
        }
    }

    // CM reset entity age is connected to making a hurt noise
    @Inject(method = "attackEntityFrom", at = @At("TAIL"))
    private void potionDespawnFix2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.potionsDespawnFix) {
            this.idleTime = 0;
        }
    }
}
