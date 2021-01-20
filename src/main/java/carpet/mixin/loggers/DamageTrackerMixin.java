package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {
    @Inject(method = "getDeathMessage", at = @At(value = "CONSTANT", args = "stringValue=death.fell.accident."),  locals = LocalCapture.CAPTURE_FAILHARD)
    private void damageDebug(CallbackInfoReturnable<Text> cir, DamageRecord combatEntry) {
        if(LoggerRegistry.__damageDebug){ // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
            LoggerRegistry.getLogger("damageDebug").log(()-> new Text[]{
                    Messenger.s(null, "Dmg: " + combatEntry.getFallDistance())
            });
        }
    }
}
