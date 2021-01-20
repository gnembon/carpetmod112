package carpet.mixin.xpNoCooldown;

import carpet.CarpetSettings;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Redirect(method = "onPlayerCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;experiencePickUpDelay:I", ordinal = 0))
    private int getCooldown(PlayerEntity player) {
        return CarpetSettings.xpNoCooldown ? 0 : player.experiencePickUpDelay;
    }
}
