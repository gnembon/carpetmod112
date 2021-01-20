package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 1))
    private void modifyDamageOnRespawn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage(this, source, amount, 0, "respawn protection");
    }

    @Inject(method = "damage", at = {@At(value = "RETURN", ordinal = 2), @At(value = "RETURN", ordinal = 3)})
    private void modifyDamageOnDisabledPVP(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage(this, source, amount, 0, "PVP disabled");
    }
}
