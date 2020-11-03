package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    @Shadow protected float lastDamage;

    @Shadow protected abstract float applyArmorCalculations(DamageSource source, float damage);

    @Shadow public abstract int getTotalArmorValue();

    @Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getHealth()F"))
    private void registerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.register_damage((EntityLivingBase) (Object) this, source, amount);
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "RETURN", ordinal = 2))
    private void modifyDamageDead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, 0, "Already dead and can't take more damage");
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "RETURN", ordinal = 3))
    private void modifyDamageFire(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, 0, "Resistance to fire");
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "CONSTANT", args = "floatValue=0.75", shift = At.Shift.AFTER))
    private void modifyDamageHelmet(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, amount * 0.75f, "wearing a helmet");
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;damageShield(F)V", shift = At.Shift.AFTER))
    private void modifyDamageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, 0, "using a shield");
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;damageEntity(Lnet/minecraft/util/DamageSource;F)V", ordinal = 0))
    private void modifyDamageRecentlyHitReduced(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, amount - this.lastDamage, "Recently hit");
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"), slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;hurtResistantTime:I", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;damageEntity(Lnet/minecraft/util/DamageSource;F)V", ordinal = 0)
    ))
    private void modifyDamageRecentlyHitNone(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, amount, 0, "Recently hit");
    }

    @Inject(method = "applyPotionDamageCalculations", at = @At(value = "CONSTANT", args = "floatValue=25"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageResistanceEffect(DamageSource source, float damage, CallbackInfoReturnable<Float> cir, int i, int j, float f) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, damage, f / 25.0F, "Resistance status effect");
    }

    @Redirect(method = "applyPotionDamageCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float getDamageAfterMagicAbsorb(float damage, float enchantModifiers, DamageSource source) {
        float after = CombatRules.getDamageAfterMagicAbsorb(damage, enchantModifiers);
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, damage, after, String.format("enchantments (%.1f total points)", enchantModifiers));
        return after;
    }

    @Redirect(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;applyArmorCalculations(Lnet/minecraft/util/DamageSource;F)F"))
    private float applyArmorCalculationsAndLog(EntityLivingBase entity, DamageSource source, float damage) {
        float after = applyArmorCalculations(source, damage);
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, source, damage, after, String.format("Armour %.1f, Toughness %.1f", (float) this.getTotalArmorValue(), this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
        return after;
    }

    @Inject(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageAbsorption(DamageSource damageSrc, float damageAmount, CallbackInfo ci, float f) {
        DamageReporter.modify_damage((EntityLivingBase) (Object) this, damageSrc, damageAmount, f, "Absorption");
    }

    @Inject(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setHealth(F)V"))
    private void registerFinalDamage(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        DamageReporter.register_final_damage((EntityLivingBase) (Object) this, damageSrc, damageAmount);
    }
}
