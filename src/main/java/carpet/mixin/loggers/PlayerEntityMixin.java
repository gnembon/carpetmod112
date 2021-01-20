package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.DamageReporter;
import carpet.logging.logHelpers.KillLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow protected abstract void damageArmor(float damage);

    private float damagePreDifficulty;
    private int mobsSmashed;
    private boolean sweeping;

    public PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;", ordinal = 0))
    private void saveDamagePreDifficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damagePreDifficulty = amount;
    }

    @Inject(method = "damage", at = @At(value = "CONSTANT", args = "floatValue=0", ordinal = 2))
    private void modifyDamageDifficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isScaledWithDifficulty()) {
            DamageReporter.modify_damage(this, source, damagePreDifficulty, amount, "difficulty");
        }
    }

    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float modifyDamageArmor(PlayerEntity entityPlayer, DamageSource source, float damage) {
        float modified = applyArmorToDamage(source, damage);
        if (LoggerRegistry.__damage) {
            DamageReporter.modify_damage(this, source, damage, modified,
                String.format("armour %.1f and toughness %.1f", (float)this.getArmor(), (float)this.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue()));
        }
        return modified;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageAbsorption(DamageSource damageSrc, float damageAmount, CallbackInfo ci, float f) {
        DamageReporter.modify_damage(this, damageSrc, damageAmount, f, "Absorption");
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V"))
    private void logFinalDamage(DamageSource source, float amount, CallbackInfo ci) {
        DamageReporter.register_final_damage(this, source, amount);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void logInvulnerable(DamageSource source, float amount, CallbackInfo ci) {
        if (LoggerRegistry.__damage && isInvulnerableTo(source)) {
            DamageReporter.modify_damage(this, source, amount, 0, "invulnerability to the damage source");
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void resetMobsSmashed(Entity targetEntity, CallbackInfo ci) {
        mobsSmashed = 1;
        sweeping = false;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void onAttackingEntity(Entity targetEntity, CallbackInfo ci) {
        mobsSmashed++;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_25033()V"))
    private void onSweep(Entity targetEntity, CallbackInfo ci) {
        sweeping = true;
        if (LoggerRegistry.__kills) KillLogHelper.onSweep((PlayerEntity) (Object) this, mobsSmashed);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackEnd(Entity targetEntity, CallbackInfo ci) {
        if (!LoggerRegistry.__kills || sweeping || !targetEntity.isAttackable()) return;
        if (!targetEntity.handleAttack(this)) {
            KillLogHelper.onNonSweepAttack((PlayerEntity) (Object) this);
        } else {
            KillLogHelper.onDudHit((PlayerEntity) (Object) this);
        }
    }
}
