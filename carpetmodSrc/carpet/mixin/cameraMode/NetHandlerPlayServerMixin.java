package carpet.mixin.cameraMode;

import carpet.CarpetSettings;
import carpet.utils.extensions.CameraPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static carpet.commands.CommandGMS.setPlayerToSurvival;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onDisconnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    private void onLogout(ITextComponent reason, CallbackInfo ci) {
        // Fix exploit related to camera mode and logging out CARPET-XCOM
        if(CarpetSettings.cameraModeRestoreLocation && ((CameraPlayer) player).getGamemodeCamera()){
            setPlayerToSurvival(server, player,true);
        }
    }

    @Inject(method = "handleSpectate", at = @At("HEAD"), cancellable = true)
    private void onSpectate(CPacketSpectate packetIn, CallbackInfo ci) {
        // Disables spectating other players when using /c and carpet rule cameraModeDisableSpectatePlayers is true CARPET-XCOM
        if (((CameraPlayer) player).isDisableSpectatePlayers()) ci.cancel();
    }
}
