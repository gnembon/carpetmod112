package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedItem;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ExtendedItemStack {
    @Shadow private int stackSize;
    @Shadow public abstract Item getItem();
    @Shadow public abstract boolean hasTagCompound();

    @Override
    public boolean isGroundStackable() {
        return ((ExtendedItem) this.getItem()).itemGroundStacking(hasTagCompound());
    }

    @Override
    public void forceStackSize(int size) {
        this.stackSize = size;
    }

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void stackableShulkersPlayerInventory(CallbackInfoReturnable<Integer> cir) {
        if (!CarpetSettings.stackableShulkersPlayerInventory || !CarpetServer.playerInventoryStacking) return;
        if (getItem() instanceof ItemShulkerBox && !hasTagCompound()) cir.setReturnValue(64);
    }
}
