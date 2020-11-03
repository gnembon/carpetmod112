package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.DamageReporter;
import carpet.logging.logHelpers.KillLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    @Shadow protected abstract void damageArmor(float damage);

    private float damagePreDifficulty;
    private int mobsSmashed;
    private boolean sweeping;

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/EnumDifficulty;", ordinal = 0))
    private void saveDamagePreDifficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damagePreDifficulty = amount;
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "CONSTANT", args = "floatValue=0", ordinal = 2))
    private void modifyDamageDifficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isDifficultyScaled()) {
            DamageReporter.modify_damage(this, source, damagePreDifficulty, amount, "difficulty");
        }
    }

    @Redirect(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;applyArmorCalculations(Lnet/minecraft/util/DamageSource;F)F"))
    private float modifyDamageArmor(EntityPlayer entityPlayer, DamageSource source, float damage) {
        float modified = applyArmorCalculations(source, damage);
        if (LoggerRegistry.__damage) {
            DamageReporter.modify_damage(this, source, damage, modified,
                String.format("armour %.1f and toughness %.1f", (float)this.getTotalArmorValue(), (float)this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
        }
        return modified;
    }

    @Inject(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyDamageAbsorption(DamageSource damageSrc, float damageAmount, CallbackInfo ci, float f) {
        DamageReporter.modify_damage(this, damageSrc, damageAmount, f, "Absorption");
    }

    @Inject(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setHealth(F)V"))
    private void logFinalDamage(DamageSource source, float amount, CallbackInfo ci) {
        DamageReporter.register_final_damage(this, source, amount);
    }

    @Inject(method = "damageEntity", at = @At("HEAD"))
    private void logInvulnerable(DamageSource source, float amount, CallbackInfo ci) {
        if (LoggerRegistry.__damage && isEntityInvulnerable(source)) {
            DamageReporter.modify_damage(this, source, amount, 0, "invulnerability to the damage source");
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    private void resetMobsSmashed(Entity targetEntity, CallbackInfo ci) {
        mobsSmashed = 1;
        sweeping = false;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private void onAttackingEntity(Entity targetEntity, CallbackInfo ci) {
        mobsSmashed++;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;spawnSweepParticles()V"))
    private void onSweep(Entity targetEntity, CallbackInfo ci) {
        sweeping = true;
        if (LoggerRegistry.__kills) KillLogHelper.onSweep((EntityPlayer) (Object) this, mobsSmashed);
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("RETURN"))
    private void onAttackEnd(Entity targetEntity, CallbackInfo ci) {
        if (!LoggerRegistry.__kills || sweeping || !targetEntity.canBeAttackedWithItem()) return;
        if (!targetEntity.hitByEntity(this)) {
            KillLogHelper.onNonSweepAttack((EntityPlayer) (Object) this);
        } else {
            KillLogHelper.onDudHit((EntityPlayer) (Object) this);
        }
    }
}
