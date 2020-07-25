package carpet.mixin.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tick()V"))
    private void fixUpdateSuppressionCrashTick(WorldServer worldServer) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            worldServer.tick();
            return;
        }
        try {
            worldServer.tick();
        } catch (ReportedException e) {
            if (!(e.getCrashReport().getCrashCause() instanceof ThrowableSuppression)) throw e;
        } catch (ThrowableSuppression ignored) {}
    }
    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateEntities()V"))
    private void fixUpdateSuppressionCrashTickEntities(WorldServer worldServer) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            worldServer.updateEntities();
            return;
        }
        try {
            worldServer.updateEntities();
        } catch (ReportedException e) {
            if (!(e.getCrashReport().getCrashCause() instanceof ThrowableSuppression)) throw e;
        } catch (ThrowableSuppression ignored) {}
    }
}
