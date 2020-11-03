package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySlime.class)
public class EntitySlimeMixin {
    @Redirect(method = "dealDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean logDamage(EntityLivingBase entityLivingBase, DamageSource source, float amount) {
        DamageReporter.register_damage_attacker(entityLivingBase, (EntitySlime) (Object) this, amount);
        return entityLivingBase.attackEntityFrom(source, amount);
    }
}
