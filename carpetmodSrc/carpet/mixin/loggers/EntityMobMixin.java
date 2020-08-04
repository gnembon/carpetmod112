package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityMob.class)
public class EntityMobMixin {
    private static final ThreadLocal<Float> attackDamagePre = new ThreadLocal<>();

    @Inject(method = "attackEntityAsMob", at = @At(value = "CONSTANT", args = "intValue=0"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onAttack(Entity entity, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageReporter.register_damage_attacker(entity, (EntityMob) (Object) this, attackDamage);
        attackDamagePre.set(attackDamage);
    }

    @Inject(method = "attackEntityAsMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockbackModifier(Lnet/minecraft/entity/EntityLivingBase;)I"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onAttackModified(Entity entity, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageReporter.modify_damage((EntityLivingBase)entity, DamageSource.causeMobDamage((EntityMob) (Object) this), attackDamagePre.get(), attackDamage, "attacker enchants");
    }
}
