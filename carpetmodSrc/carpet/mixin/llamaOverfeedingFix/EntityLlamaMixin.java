package carpet.mixin.llamaOverfeedingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.passive.EntityLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityLlama.class)
public class EntityLlamaMixin {
    @Redirect(method = "handleEating", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityLlama;isTame()Z", ordinal = 0))
    private boolean isTameOrOverfeeding(EntityLlama llama) {
        return llama.isTame() && !(CarpetSettings.llamaOverfeedingFix && llama.isInLove());
    }
}
