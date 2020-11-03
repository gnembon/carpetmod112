package carpet.mixin.combineXPOrbs;

import carpet.CarpetSettings;
import carpet.helpers.XPcombine;
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityXPOrb.class)
public class EntityXPOrbMixin {
    private int delayBeforeCombine = 50;

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityXPOrb;move(Lnet/minecraft/entity/MoverType;DDD)V", shift = At.Shift.AFTER))
    private void tryMerge(CallbackInfo ci) {
        if (CarpetSettings.combineXPOrbs) {
            if (this.delayBeforeCombine > 0) {
                --this.delayBeforeCombine;
            }
            XPcombine.searchForOtherXPNearbyCarpet((EntityXPOrb) (Object) this);
        }
    }
}
