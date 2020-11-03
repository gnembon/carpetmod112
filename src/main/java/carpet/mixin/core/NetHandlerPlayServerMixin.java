package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processCustomPayload", at = @At("TAIL"))
    private void handleCarpetCustomPayloads(CPacketCustomPayload packet, CallbackInfo ci) {
        if (!packet.getChannelName().startsWith("MC|")) {
            CarpetServer.pluginChannels.process(player, packet);
        }
    }

    @Inject(method = "processInput", at = @At("RETURN"))
    private void resetActiveTimeout(CPacketInput packet, CallbackInfo ci) {
        if (packet.getStrafeSpeed() != 0.0F || packet.getForwardSpeed() != 0.0F || packet.isJumping() || packet.isSneaking()) {
            TickSpeed.reset_player_active_timeout();
        }
    }

    @Inject(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isPlayerSleeping()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void resetActiveTimeout(CPacketPlayer packet, CallbackInfo ci, WorldServer world,
            double posX, double posY, double posZ, double posY2, double packetX, double packetY, double packetZ,
            float packetYaw, float packetPitch, double diffX, double diffY, double diffZ, double speed, double distance) {
        if (distance > 0.0001) {
            TickSpeed.reset_player_active_timeout();
        }
    }

    // Invisibility fix
    @Redirect(method = "handleSpectate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;removeEntityDangerously(Lnet/minecraft/entity/Entity;)V"))
    private void removeEntityDangerously(WorldServer world, Entity player) {
        world.removeEntity(player);
        world.getChunk(player.chunkCoordX, player.chunkCoordZ).removeEntityAtIndex(player, player.chunkCoordY);
    }

    @Inject(method = "handleSpectate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private void updateRemovedPlayer(CPacketSpectate packet, CallbackInfo ci) {
        player.getServerWorld().updateEntityWithOptionalForce(player, false);
    }
}
