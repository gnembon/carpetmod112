package carpet.mixin.autoCraftingDropper;

import carpet.CarpetSettings;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileEntityDropper.class)
public class TileEntityDropperMixin extends TileEntityDispenser {
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (CarpetSettings.autoCraftingDropper && world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.DROPPER && world.getBlockState(pos.offset(state.getValue(BlockDispenser.FACING))).getBlock() == Blocks.CRAFTING_TABLE) {
                return this.getItems().get(slot).isEmpty();
            }
        }
        return super.isItemValidForSlot(slot, stack);
    }
}
