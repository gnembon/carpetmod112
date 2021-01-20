package carpet.mixin.combineXPOrbs;

import carpet.CarpetSettings;
import carpet.helpers.XPcombine;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    private int delayBeforeCombine = 50;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;move(Lnet/minecraft/entity/MovementType;DDD)V", shift = At.Shift.AFTER))
    private void tryMerge(CallbackInfo ci) {
        if (CarpetSettings.combineXPOrbs) {
            if (this.delayBeforeCombine > 0) {
                --this.delayBeforeCombine;
            }
            XPcombine.searchForOtherXPNearbyCarpet((ExperienceOrbEntity) (Object) this);
        }
    }
}
