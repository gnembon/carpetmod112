package carpet.mixin.player;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.NetHandlerPlayServerFake;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private List<EntityPlayerMP> playerEntityList;

    @Inject(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;setWorld(Lnet/minecraft/world/WorldServer;)V"))
    private void resetToSetPosition(NetworkManager netManager, EntityPlayerMP player, CallbackInfo ci) {
        if (player instanceof EntityPlayerMPFake) {
            // Ignore position from NBT and use the one specified in the command
            ((EntityPlayerMPFake) player).resetToSetPosition();
        }
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
    private NetHandlerPlayServer createNetHandler(MinecraftServer server, NetworkManager netManager, EntityPlayerMP player) {
        if (player instanceof EntityPlayerMPFake) {
            return new NetHandlerPlayServerFake(server, netManager, player);
        }
        return new NetHandlerPlayServer(server, netManager, player);
    }

    @Inject(method = "saveAllPlayerData", at = @At("HEAD"))
    private void storeFakePlayerData(CallbackInfo ci) {
        ArrayList<String> list = new ArrayList<>();
        if(!CarpetSettings.reloadFakePlayers){
            CarpetServer.writeConf(server, list);
            return;
        }
        for (EntityPlayerMP p : playerEntityList) {
            if(p instanceof EntityPlayerMPFake){
                list.add(EntityPlayerMPFake.getInfo(p));
            }
        }
        CarpetServer.writeConf(server, list);
    }
}
