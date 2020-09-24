package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {
    @Inject(method = "getDeathMessage", at = @At(value = "CONSTANT", args = "stringValue=death.fell.accident."),  locals = LocalCapture.CAPTURE_FAILHARD)
    private void damageDebug(CallbackInfoReturnable<ITextComponent> cir, CombatEntry combatEntry) {
        if(LoggerRegistry.__damageDebug){ // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
            LoggerRegistry.getLogger("damageDebug").log(()-> new ITextComponent[]{
                    Messenger.s(null, "Dmg: " + combatEntry.getDamageAmount())
            });
        }
    }
}
