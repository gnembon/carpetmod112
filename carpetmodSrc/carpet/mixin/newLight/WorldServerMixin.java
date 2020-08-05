package carpet.mixin.newLight;

import carpet.CarpetSettings;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public class WorldServerMixin extends WorldMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
    private void procLightUpdates(CallbackInfo ci) {
        if (!CarpetSettings.newLight) return;
        this.profiler.endStartSection("lighting");
        this.lightingEngine.procLightUpdates();
    }
}
