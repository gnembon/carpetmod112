package carpet.mixin.potions;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Redirect(method = "onFinishedPotionEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;onFinishedPotionEffect(Lnet/minecraft/potion/PotionEffect;)V"))
    private void finishPotionEffectHead(EntityPlayer entityPlayer, PotionEffect effect) {
        if (!CarpetSettings.effectsFix) super.onFinishedPotionEffect(effect);
    }

    @Inject(method = "onFinishedPotionEffect", at = @At("RETURN"))
    private void finishedPotionEffectReturn(PotionEffect effect, CallbackInfo ci) {
        if (CarpetSettings.effectsFix) super.onFinishedPotionEffect(effect);
    }
}
