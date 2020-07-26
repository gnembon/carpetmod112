package carpet.mixin.stackableShulkerBoxes;

import carpet.utils.extensions.ExtendedItem;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}
