package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnchantmentTable.class)
public class TileEntityEnchantmentTableMixin extends TileEntity {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void sleepOnServer(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && !world.isRemote) ci.cancel();
    }
}
