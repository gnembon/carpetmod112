package carpet.mixin.potions;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Redirect(method = "onStatusEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private void finishPotionEffectHead(PlayerEntity entityPlayer, StatusEffectInstance effect) {
        if (!CarpetSettings.effectsFix) super.onStatusEffectRemoved(effect);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("RETURN"))
    private void finishedPotionEffectReturn(StatusEffectInstance effect, CallbackInfo ci) {
        if (CarpetSettings.effectsFix) super.onStatusEffectRemoved(effect);
    }
}
