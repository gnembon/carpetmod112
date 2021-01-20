package carpet.mixin.ctrlQCraftingFix;

import carpet.CarpetSettings;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {
    @Shadow public List<Slot> field_22767;
    @Shadow public abstract ItemStack onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player);
    @Shadow public abstract void sendContentUpdates();

    @Inject(method = "onSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;", ordinal = 2), cancellable = true)
    private void ctrlQCrafting(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (CarpetSettings.ctrlQCraftingFix && slotId == 0 && dragType == 1) {
            Slot slot = this.field_22767.get(slotId);
            while (slot.hasStack()) {
                this.onSlotClick(slotId, 0, SlotActionType.THROW, player);
            }
            this.sendContentUpdates();
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
