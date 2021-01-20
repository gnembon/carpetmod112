package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Shadow protected abstract Inventory getOutputInventory();

    @Shadow public static Inventory getInputInventory(Hopper hopper) { throw new AbstractMethodError(); }

    @Redirect(method = "insert", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getOutputInventory()Lnet/minecraft/inventory/Inventory;"))
    private Inventory getInventoryForHopperTransferAndLog(HopperBlockEntity hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper loading");
            return getOutputInventory();
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Redirect(method = "extract(Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputInventory(Lnet/minecraft/block/entity/Hopper;)Lnet/minecraft/inventory/Inventory;"))
    private static Inventory getSourceInventoryAndLog(Hopper hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper self-loading");
            return getInputInventory(hopper);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }
}
