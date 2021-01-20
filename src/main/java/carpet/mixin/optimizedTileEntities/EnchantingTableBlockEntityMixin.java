package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public class EnchantingTableBlockEntityMixin extends BlockEntity {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sleepOnServer(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && !world.isClient) ci.cancel();
    }
}
