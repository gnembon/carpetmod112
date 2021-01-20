package carpet.mixin.newLight;

import carpet.CarpetSettings;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin extends WorldMixin {
    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void procLightUpdates(CallbackInfo ci) {
        if (!CarpetSettings.newLight) return;
        this.profiler.swap("lighting");
        this.lightingEngine.procLightUpdates();
    }
}
