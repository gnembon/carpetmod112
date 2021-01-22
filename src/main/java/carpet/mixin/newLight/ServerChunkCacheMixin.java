package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
    @Shadow @Final private ServerWorld world;

    @Inject(method = "method_33450", at = @At("HEAD"))
    private void procLightOnSave(boolean all, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.newLight) {
            ((NewLightWorld) world).getLightingEngine().procLightUpdates();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void procLightOnUnload(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.newLight) {
            ((NewLightWorld) world).getLightingEngine().procLightUpdates();
        }
    }
}
