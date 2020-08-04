package carpet.mixin.autoCraftingDropper;

import carpet.CarpetSettings;
import carpet.helpers.AutoCraftingDropperHelper;
import carpet.utils.VoidContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockDropper.class)
public class BlockDropperMixin extends BlockDispenser {
    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private void autoCraftOnDispense(World worldIn, BlockPos pos, CallbackInfo ci) {
        if (CarpetSettings.autoCraftingDropper && this.autoCraftingDispense(worldIn, pos)) ci.cancel();
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        if (CarpetSettings.autoCraftingDropper) {
            BlockPos front = pos.offset(worldIn.getBlockState(pos).getValue(BlockDispenser.FACING));
            if (worldIn.getBlockState(front).getBlock() == Blocks.CRAFTING_TABLE) {
                TileEntityDispenser dispenserTE = (TileEntityDispenser) worldIn.getTileEntity(pos);
                if (dispenserTE != null) {
                    int filled = 0;
                    for (ItemStack stack : dispenserTE.getItems()) {
                        if (!stack.isEmpty()) filled++;
                    }
                    return (filled * 15) / 9;
                }
            }
        }
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }

    private boolean autoCraftingDispense(World worldIn, BlockPos pos) {
        BlockPos front = pos.offset(worldIn.getBlockState(pos).getValue(BlockDispenser.FACING));
        if (worldIn.getBlockState(front).getBlock() != Blocks.CRAFTING_TABLE) {
            return false;
        }
        TileEntityDispenser dispenserTE = (TileEntityDispenser) worldIn.getTileEntity(pos);
        if (dispenserTE == null) {
            return false;
        }
        InventoryCrafting craftingInventory = new InventoryCrafting(new VoidContainer(), 3, 3);
        for (int i = 0; i < 9; i++) {
            craftingInventory.setInventorySlotContents(i, dispenserTE.getStackInSlot(i));
        }
        IRecipe recipe = CraftingManager.findMatchingRecipe(craftingInventory, worldIn);
        if (recipe == null) {
            return false;
        }
        // crafting it
        Vec3d target = new Vec3d(front).add(0.5, 0.2, 0.5);
        ItemStack result = recipe.getCraftingResult(craftingInventory);
        AutoCraftingDropperHelper.spawnItemStack(worldIn, target.x, target.y, target.z, result);

        // copied from CraftingResultSlot.onTakeItem()
        NonNullList<ItemStack> nonNullList = recipe.getRemainingItems(craftingInventory);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack_2 = dispenserTE.getStackInSlot(i);
            ItemStack itemStack_3 = nonNullList.get(i);
            if (!itemStack_2.isEmpty()) {
                dispenserTE.decrStackSize(i, 1);
                itemStack_2 = dispenserTE.getStackInSlot(i);
            }

            if (!itemStack_3.isEmpty()) {
                if (itemStack_2.isEmpty()) {
                    dispenserTE.setInventorySlotContents(i, itemStack_3);
                } else if (ItemStack.areItemsEqualIgnoreDurability(itemStack_2, itemStack_3) && ItemStack.areItemStackTagsEqual(itemStack_2, itemStack_3)) {
                    itemStack_3.grow(itemStack_2.getCount());
                    dispenserTE.setInventorySlotContents(i, itemStack_3);
                } else {
                    AutoCraftingDropperHelper.spawnItemStack(worldIn, target.x, target.y, target.z, itemStack_3);
                }
            }
        }
        return true;
    }
}
