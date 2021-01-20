package carpet.mixin.autoCraftingDropper;

import carpet.CarpetSettings;
import carpet.helpers.AutoCraftingDropperHelper;
import carpet.mixin.accessors.DispenserBlockEntityAccessor;
import carpet.utils.VoidContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DropperBlock.class)
public class DropperBlockMixin extends DispenserBlock {
    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private void autoCraftOnDispense(World worldIn, BlockPos pos, CallbackInfo ci) {
        if (CarpetSettings.autoCraftingDropper && this.autoCraftingDispense(worldIn, pos)) ci.cancel();
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos) {
        if (CarpetSettings.autoCraftingDropper) {
            BlockPos front = pos.offset(worldIn.getBlockState(pos).get(DispenserBlock.FACING));
            if (worldIn.getBlockState(front).getBlock() == Blocks.CRAFTING_TABLE) {
                DispenserBlockEntity dispenserTE = (DispenserBlockEntity) worldIn.getBlockEntity(pos);
                if (dispenserTE != null) {
                    int filled = 0;
                    for (ItemStack stack : ((DispenserBlockEntityAccessor) dispenserTE).getInventory()) {
                        if (!stack.isEmpty()) filled++;
                    }
                    return (filled * 15) / 9;
                }
            }
        }
        return super.getComparatorOutput(blockState, worldIn, pos);
    }

    private boolean autoCraftingDispense(World worldIn, BlockPos pos) {
        BlockPos front = pos.offset(worldIn.getBlockState(pos).get(DispenserBlock.FACING));
        if (worldIn.getBlockState(front).getBlock() != Blocks.CRAFTING_TABLE) {
            return false;
        }
        DispenserBlockEntity dispenserTE = (DispenserBlockEntity) worldIn.getBlockEntity(pos);
        if (dispenserTE == null) {
            return false;
        }
        CraftingInventory craftingInventory = new CraftingInventory(new VoidContainer(), 3, 3);
        for (int i = 0; i < 9; i++) {
            craftingInventory.setInvStack(i, dispenserTE.getInvStack(i));
        }
        Recipe recipe = RecipeManager.method_25728(craftingInventory, worldIn);
        if (recipe == null) {
            return false;
        }
        // crafting it
        Vec3d target = new Vec3d(front).add(0.5, 0.2, 0.5);
        ItemStack result = recipe.method_25712(craftingInventory);
        AutoCraftingDropperHelper.spawnItemStack(worldIn, target.x, target.y, target.z, result);

        // copied from CraftingResultSlot.onTakeItem()
        DefaultedList<ItemStack> nonNullList = recipe.method_25715(craftingInventory);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack_2 = dispenserTE.getInvStack(i);
            ItemStack itemStack_3 = nonNullList.get(i);
            if (!itemStack_2.isEmpty()) {
                dispenserTE.takeInvStack(i, 1);
                itemStack_2 = dispenserTE.getInvStack(i);
            }

            if (!itemStack_3.isEmpty()) {
                if (itemStack_2.isEmpty()) {
                    dispenserTE.setInvStack(i, itemStack_3);
                } else if (ItemStack.areItemsEqual(itemStack_2, itemStack_3) && ItemStack.areTagsEqual(itemStack_2, itemStack_3)) {
                    itemStack_3.increment(itemStack_2.getCount());
                    dispenserTE.setInvStack(i, itemStack_3);
                } else {
                    AutoCraftingDropperHelper.spawnItemStack(worldIn, target.x, target.y, target.z, itemStack_3);
                }
            }
        }
        return true;
    }
}
