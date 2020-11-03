package carpet.mixin.stackableShulkerBoxes;

import carpet.utils.extensions.ExtendedItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements ExtendedItem {
    public boolean itemGroundStacking(boolean hasTagCompound) {
        return false;
    }
}
