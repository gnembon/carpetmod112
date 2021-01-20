package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedItem;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShulkerBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ExtendedItemStack {
    @Shadow private int count;
    @Shadow public abstract Item getItem();
    @Shadow public abstract boolean hasTag();

    @Override
    public boolean isGroundStackable() {
        return ((ExtendedItem) this.getItem()).itemGroundStacking(hasTag());
    }

    @Override
    public void forceStackSize(int size) {
        this.count = size;
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void stackableShulkersPlayerInventory(CallbackInfoReturnable<Integer> cir) {
        if (!CarpetSettings.stackableShulkersPlayerInventory || !CarpetServer.playerInventoryStacking.get()) return;
        if (getItem() instanceof ShulkerBoxItem && !hasTag()) cir.setReturnValue(64);
    }
}
