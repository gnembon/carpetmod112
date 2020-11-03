package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySkull.class)
public class TileEntitySkullMixin extends TileEntity {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void optimize(CallbackInfo ci) {
        // Skip update on servers, because it only performs an animation
        if (CarpetSettings.optimizedTileEntities && !world.isRemote) ci.cancel();
    }
}
