package carpet.mixin.antiCheatSpeed;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isInvulnerableDimensionChange()Z"))
    private boolean antiCheatSpeed(EntityPlayerMP player) {
        return CarpetSettings.antiCheatSpeed || player.isInvulnerableDimensionChange();
    }
}
