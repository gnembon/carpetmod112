package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public class SkullBlockEntityMixin extends BlockEntity {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void optimize(CallbackInfo ci) {
        // Skip update on servers, because it only performs an animation
        if (CarpetSettings.optimizedTileEntities && !world.isClient) ci.cancel();
    }
}
