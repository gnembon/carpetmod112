package carpet.mixin.profiler;

import carpet.utils.CarpetProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=save"))
    private void onAutosaveStart(CallbackInfo ci) {
        CarpetProfiler.start_section(null, "Autosave");
    }

    @Inject(method = "tick",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=save")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.AFTER))
    private void onAutosaveEnd(CallbackInfo ci) {
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=connection"))
    private void onNetworkStart(CallbackInfo ci) {
        CarpetProfiler.start_section(null, "Network");
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=commandFunctions"))
    private void onNetworkEnd(CallbackInfo ci) {
        CarpetProfiler.end_current_section();
    }
}
