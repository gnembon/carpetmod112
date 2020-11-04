package carpet.mixin.carefulBreak;

import carpet.helpers.CarefulBreakHelper;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.FutureTask;

@Mixin(Util.class)
public class UtilMixin {
    @Inject(method = "runTask", at = @At(value = "RETURN", ordinal = 1))
    private static void clearPlayerMinedBlock(FutureTask<?> task, Logger logger, CallbackInfoReturnable<?> cir) {
        CarefulBreakHelper.miningPlayer = null;
    }
}
