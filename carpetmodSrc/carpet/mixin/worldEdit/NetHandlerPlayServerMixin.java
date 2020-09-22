package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processTryUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;processRightClick(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;"))
    private void onRightClickAir(CPacketPlayerTryUseItem packetIn, CallbackInfo ci) {
        WorldEditBridge.onRightClickAir(server.getWorld(player.dimension), player);
    }
}
