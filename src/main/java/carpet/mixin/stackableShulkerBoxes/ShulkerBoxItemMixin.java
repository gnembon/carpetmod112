package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ShulkerBoxItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxItem.class)
public class ShulkerBoxItemMixin extends BlockItem implements ExtendedItem {
    public ShulkerBoxItemMixin(Block block) {
        super(block);
    }

    @Override
    public boolean itemGroundStacking(boolean hasTagCompound) {
        return !hasTagCompound && CarpetSettings.stackableEmptyShulkerBoxes;
    }
}
