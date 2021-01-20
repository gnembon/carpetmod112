package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import carpet.utils.TickingArea;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "prepareStartRegion", at = @At("HEAD"))
    private void initialTickingAreaChunkLoad(CallbackInfo ci) {
        if (!CarpetSettings.tickingAreas) return;
        TickingArea.initialChunkLoad((MinecraftServer) (Object) this, true);
    }
}
