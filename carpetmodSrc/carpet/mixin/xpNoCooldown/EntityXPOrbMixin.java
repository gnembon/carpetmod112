package carpet.mixin.xpNoCooldown;

import carpet.CarpetSettings;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityXPOrb.class)
public class EntityXPOrbMixin {
    @Redirect(method = "onCollideWithPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;xpCooldown:I", ordinal = 0))
    private int getCooldown(EntityPlayer player) {
        return CarpetSettings.xpNoCooldown ? 0 : player.xpCooldown;
    }
}
