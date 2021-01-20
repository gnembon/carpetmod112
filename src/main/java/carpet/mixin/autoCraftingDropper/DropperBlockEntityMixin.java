package carpet.mixin.autoCraftingDropper;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DropperBlockEntity.class)
public class DropperBlockEntityMixin extends DispenserBlockEntity {
    @Override
    public boolean isValidInvStack(int slot, ItemStack stack) {
        if (CarpetSettings.autoCraftingDropper && world != null) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.DROPPER && world.getBlockState(pos.offset(state.get(DispenserBlock.FACING))).getBlock() == Blocks.CRAFTING_TABLE) {
                return this.getInvStackList().get(slot).isEmpty();
            }
        }
        return super.isValidInvStack(slot, stack);
    }
}
