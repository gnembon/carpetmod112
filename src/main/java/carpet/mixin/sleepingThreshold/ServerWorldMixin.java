package carpet.mixin.sleepingThreshold;

import carpet.CarpetSettings;
import net.minecraft.class_1268;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(class_1268 levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Redirect(method = "updatePlayersSleeping", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false))
    private int getPlayerListSize(List<PlayerEntity> list) {
        return CarpetSettings.sleepingThreshold < 100 ? 0 : list.size();
    }

    @Inject(method = "method_33479", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false), cancellable = true)
    private void sleepingThreshold(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.sleepingThreshold < 100) {
            int players = 0;
            int sleeping = 0;
            for (PlayerEntity player : field_23576) {
                if (player.isSpectator()) continue;
                players++;
                if (player.isSleepingLongEnough()) sleeping++;
            }
            cir.setReturnValue(players == 0 || CarpetSettings.sleepingThreshold * players <= sleeping * 100);
        }
    }
}
