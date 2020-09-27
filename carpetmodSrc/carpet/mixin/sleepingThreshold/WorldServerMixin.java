package carpet.mixin.sleepingThreshold;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Redirect(method = "updateAllPlayersSleepingFlag", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false))
    private int getPlayerListSize(List<EntityPlayer> list) {
        return CarpetSettings.sleepingThreshold < 100 ? 0 : list.size();
    }

    @Inject(method = "areAllPlayersAsleep", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false), cancellable = true)
    private void sleepingThreshold(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.sleepingThreshold < 100) {
            int players = 0;
            int sleeping = 0;
            for (EntityPlayer player : playerEntities) {
                if (player.isSpectator()) continue;
                players++;
                if (player.isPlayerFullyAsleep()) sleeping++;
            }
            cir.setReturnValue(players == 0 || CarpetSettings.sleepingThreshold * players <= sleeping * 100);
        }
    }
}
