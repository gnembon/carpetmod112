package carpet.mixin.mobsDontControlMinecarts;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityMinecart.class)
public class EntityMinecartMixin {
    @Redirect(method = "moveAlongTrack", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;moveForward:F"))
    private float mobsDontControlMinecarts(EntityLivingBase entity) {
        if (!CarpetSettings.mobsDontControlMinecarts || entity instanceof EntityPlayer) return entity.moveForward;
        return 0;
    }
}
