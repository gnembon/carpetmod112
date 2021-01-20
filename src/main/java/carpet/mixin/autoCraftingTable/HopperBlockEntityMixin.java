package carpet.mixin.autoCraftingTable;

import carpet.CarpetSettings;
import carpet.helpers.CraftingTableBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/inventory/Inventory;getInvStack(I)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void cancelAutoCraftingItemFilter(Hopper hopper, Inventory input, int index, Direction direction, CallbackInfoReturnable<Boolean> cir, ItemStack stack) {
        // Added method to fix auto crafting sucking out items to early CAPRET-XCOM
        if (CarpetSettings.autocrafter && CraftingTableBlockEntity.checkIfCanCraft(hopper, input, stack)) {
            cir.setReturnValue(false);
        }
    }
}
