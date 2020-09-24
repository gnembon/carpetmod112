package carpet.mixin.itemDesynchFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketClickWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processClickWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isChangingQuantityOnly:Z", ordinal = 0))
    private void itemDesynchFix(CPacketClickWindow packetIn, CallbackInfo ci) {
        // Update item changes before setting boolean true given it can cause desynchs. CARPET-XCOM
        if (CarpetSettings.itemDesynchFix) {
            this.player.openContainer.detectAndSendChanges();
        }
    }
}
