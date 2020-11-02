package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "RETURN", ordinal = 1))
    private void modifyDamageOnRespawn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage(this, source, amount, 0, "respawn protection");
    }

    @Inject(method = "attackEntityFrom", at = {@At(value = "RETURN", ordinal = 2), @At(value = "RETURN", ordinal = 3)})
    private void modifyDamageOnDisabledPVP(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage(this, source, amount, 0, "PVP disabled");
    }
}
