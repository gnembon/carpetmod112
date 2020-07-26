package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemShulkerBox.class)
public class ItemShulkerBoxMixin extends ItemBlock implements ExtendedItem {
    public ItemShulkerBoxMixin(Block block) {
        super(block);
    }

    @Override
    public boolean itemGroundStacking(boolean hasTagCompound) {
        return !hasTagCompound && CarpetSettings.stackableEmptyShulkerBoxes;
    }
}
