package carpet.mixin.elytraCheckFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    // Remove the falling check as in 1.15 CARPET-XCOM
    @Redirect(method = "processEntityAction", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;motionY:D"))
    private double motionForElytraCheck(EntityPlayerMP player) {
        return CarpetSettings.elytraCheckFix ? -1 : player.motionY;
    }
}
