package carpet.mixin.autoCraftingTable;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityCraftingTable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TileEntityHopper.class)
public class TileEntityHopperMixin {
    @Inject(method = "pullItemFromSlot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/inventory/IInventory;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void cancelAutoCraftingItemFilter(IHopper hopper, IInventory input, int index, EnumFacing direction, CallbackInfoReturnable<Boolean> cir, ItemStack stack) {
        // Added method to fix auto crafting sucking out items to early CAPRET-XCOM
        if (CarpetSettings.autocrafter && TileEntityCraftingTable.checkIfCanCraft(hopper, input, stack)) {
            cir.setReturnValue(false);
        }
    }
}
