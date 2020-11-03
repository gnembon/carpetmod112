package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin {
    @Shadow protected abstract IInventory getInventoryForHopperTransfer();

    @Shadow public static IInventory getSourceInventory(IHopper hopper) { throw new AbstractMethodError(); }

    @Redirect(method = "transferItemsOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;getInventoryForHopperTransfer()Lnet/minecraft/inventory/IInventory;"))
    private IInventory getInventoryForHopperTransferAndLog(TileEntityHopper hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper loading");
            return getInventoryForHopperTransfer();
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Redirect(method = "pullItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;getSourceInventory(Lnet/minecraft/tileentity/IHopper;)Lnet/minecraft/inventory/IInventory;"))
    private static IInventory getSourceInventoryAndLog(IHopper hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper self-loading");
            return getSourceInventory(hopper);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }
}
