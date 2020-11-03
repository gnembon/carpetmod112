package carpet.mixin.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Util.class)
public class UtilMixin {
    @Redirect(method = "runTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Throwable;)V", ordinal = 0, remap = false))
    private static void logFatal(Logger logger, String message, Throwable t) {
        if (CarpetSettings.updateSuppressionCrashFix && (t.getCause() instanceof ThrowableSuppression)) return;
        logger.fatal(message, t);
    }
}
