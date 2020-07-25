package carpet.mixin.pistonFixes;

import carpet.utils.PistonFixes;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "updateTimeLightAndEntities", at = @At("RETURN"))
    private void pistonFixOnEndTick(CallbackInfo ci) {
        PistonFixes.onEndTick();
    }
}
