package carpet.mixin.llamaOverfeedingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LlamaEntity.class)
public class LlamaEntityMixin {
    @Redirect(method = "receiveFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/LlamaEntity;isTame()Z", ordinal = 0))
    private boolean isTameOrOverfeeding(LlamaEntity llama) {
        return llama.isTame() && !(CarpetSettings.llamaOverfeedingFix && llama.isInLove());
    }
}
