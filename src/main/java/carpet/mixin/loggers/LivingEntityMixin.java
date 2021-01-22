package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected float lastDamageTaken;

    @Shadow protected abstract float applyArmorToDamage(DamageSource source, float damage);
    @Shadow public abstract int getArmor();
    @Shadow public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getHealth()F"))
    private void registerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.register_damage((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 2))
    private void modifyDamageDead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Already dead and can't take more damage");
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 3))
    private void modifyDamageFire(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Resistance to fire");
    }

    @Inject(method = "damage", at = @At(value = "CONSTANT", args = "floatValue=0.75", shift = At.Shift.AFTER))
    private void modifyDamageHelmet(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, amount * 0.75f, "wearing a helmet");
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V", shift = At.Shift.AFTER))
    private void modifyDamageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "using a shield");
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", ordinal = 0))
    private void modifyDamageRecentlyHitReduced(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, amount - this.lastDamageTaken, "Recently hit");
    }

    @Inject(method = "damage", at = @At("RETURN"), slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", ordinal = 0)
    ))
    private void modifyDamageRecentlyHitNone(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Recently hit");
    }

    @Inject(method = "applyEnchantmentsToDamage", at = @At(value = "CONSTANT", args = "floatValue=25"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageResistanceEffect(DamageSource source, float damage, CallbackInfoReturnable<Float> cir, int i, int j, float f) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, f / 25.0F, "Resistance status effect");
    }

    @Redirect(method = "applyEnchantmentsToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getInflictedDamage(FF)F"))
    private float getDamageAfterMagicAbsorb(float damage, float enchantModifiers, DamageSource source) {
        float after = DamageUtil.getInflictedDamage(damage, enchantModifiers);
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, after, String.format("enchantments (%.1f total points)", enchantModifiers));
        return after;
    }

    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float applyArmorCalculationsAndLog(LivingEntity entity, DamageSource source, float damage) {
        float after = applyArmorToDamage(source, damage);
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, after, String.format("Armour %.1f, Toughness %.1f", (float) this.getArmor(), this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()));
        return after;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageAbsorption(DamageSource damageSrc, float damageAmount, CallbackInfo ci, float f) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, damageSrc, damageAmount, f, "Absorption");
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    private void registerFinalDamage(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        DamageReporter.register_final_damage((LivingEntity) (Object) this, damageSrc, damageAmount);
    }
}
