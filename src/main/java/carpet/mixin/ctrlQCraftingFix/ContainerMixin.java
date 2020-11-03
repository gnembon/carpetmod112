package carpet.mixin.ctrlQCraftingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {
    @Shadow public List<Slot> inventorySlots;
    @Shadow public abstract ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player);
    @Shadow public abstract void detectAndSendChanges();

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;decrStackSize(I)Lnet/minecraft/item/ItemStack;", ordinal = 2), cancellable = true)
    private void ctrlQCrafting(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (CarpetSettings.ctrlQCraftingFix && slotId == 0 && dragType == 1) {
            Slot slot = this.inventorySlots.get(slotId);
            while (slot.getHasStack()) {
                this.slotClick(slotId, 0, ClickType.THROW, player);
            }
            this.detectAndSendChanges();
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
