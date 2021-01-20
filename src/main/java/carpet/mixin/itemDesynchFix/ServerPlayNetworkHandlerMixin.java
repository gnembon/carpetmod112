package carpet.mixin.itemDesynchFix;

import carpet.CarpetSettings;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onClickWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;skipPacketSlotUpdates:Z", ordinal = 0))
    private void itemDesynchFix(ClickWindowC2SPacket packetIn, CallbackInfo ci) {
        // Update item changes before setting boolean true given it can cause desynchs. CARPET-XCOM
        if (CarpetSettings.itemDesynchFix) {
            this.player.currentScreenHandler.sendContentUpdates();
        }
    }
}
