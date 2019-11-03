package carpet.helpers;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingTableBlockEntity extends TileEntityLockable implements ISidedInventory //, RecipeUnlocker, RecipeInputProvider
{
    /*
    public static final BlockEntityType<CraftingTableBlockEntity> TYPE = Registry.register(
            Registry.BLOCK_ENTITY,
            "carpet:crafting_table",
            BlockEntityType.Builder.create(CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE).build(null)
    );
     */
    private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public InventoryCrafting inventory = new InventoryCrafting(null, 3, 3);
    public ItemStack output = ItemStack.EMPTY;
    private List<AutoCraftingTableContainer> openContainers = new ArrayList<>();

    /*
    public CraftingTableBlockEntity() {  //this(BlockEntityType.BARREL);
        this(TYPE);
    }
    */

    public CraftingTableBlockEntity()
    {
        super();
    }

    public static void init()
    {
    } // registers BE type

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, inventory.stackList);
        tag.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        ItemStackHelper.loadAllItems(tag, inventory.stackList);
        this.output = new ItemStack(tag.getCompoundTag("Output"));
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation("container.crafting");
    }
    /*
    @Override
    protected Container createContainer(int id, PlayerInventory playerInventory)
    {
        AutoCraftingTableContainer container = new AutoCraftingTableContainer(id, playerInventory, this);
        this.openContainers.add(container);
        return container;
    }
    */
    // Not sure about this one so left it commented
    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        AutoCraftingTableContainer container = new AutoCraftingTableContainer(playerInventory, this, this.world, this.pos);
        this.openContainers.add(container);
        return container;
    }

    @Override
    public String getGuiID()
    {
        return "minecraft:crafting_table";
    }

    @Override
    public int[] getSlotsForFace(EnumFacing dir)
    {
        if (dir == EnumFacing.DOWN && (!output.isEmpty() || getCurrentRecipe().isPresent()))
            return OUTPUT_SLOTS;
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing dir)
    {
        return slot > 0 && getStackInSlot(slot).isEmpty();
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing dir)
    {
        if (slot == 0)
            return !output.isEmpty() || getCurrentRecipe().isPresent();
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return slot != 0;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public int getSizeInventory()
    {
        return 10;
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.isEmpty() && output.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (slot > 0)
            return this.inventory.getStackInSlot(slot - 1);
        if (!output.isEmpty())
            return output;
        Optional<IRecipe> recipe = getCurrentRecipe();
        return recipe.map(r -> r.getCraftingResult(inventory)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        if (slot == 0)
        {
            if (output.isEmpty())
            {
                output = craft();
            }
            return output.splitStack(amount);
        }
        return ItemStackHelper.getAndSplit(inventory.stackList, slot - 1, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (slot == 0)
        {
            ItemStack output = this.output;
            this.output = ItemStack.EMPTY;
            return output;
        }
        return this.inventory.removeStackFromSlot(slot - 1);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            output = stack;
            return;
        }
        inventory.stackList.set(slot - 1, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        for (AutoCraftingTableContainer c : openContainers)
            c.onCraftMatrixChanged(this);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer var1)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

//    @Override
//    public void provideRecipeInputs(RecipeFinder finder)
//    {
//        for (ItemStack stack : this.inventory)
//        {
//            finder.addItem(stack);
//        }
//    }
//
//    @Override
//    public void setLastRecipe(Recipe<?> var1)
//    {
//
//    }
//
//    @Override
//    public Recipe<?> getLastRecipe()
//    {
//        return null;
//    }

    @Override
    public void clear()
    {
        this.inventory.clear();
    }

    private Optional<IRecipe> getCurrentRecipe()
    {
        if (this.world == null)
            return Optional.empty();
        return Optional.ofNullable(CraftingManager.findMatchingRecipe(inventory, this.world));
    }

//    private ItemStack craft()
//    {
//        if (this.world == null)
//            return ItemStack.EMPTY;
//        Optional<CraftingRecipe> optionalRecipe = getCurrentRecipe();
//        if (!optionalRecipe.isPresent())
//            return ItemStack.EMPTY;
//        CraftingRecipe recipe = optionalRecipe.get();
//        ItemStack result = recipe.craft(craftingInventory);
//        DefaultedList<ItemStack> remaining = world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, craftingInventory, world);
//        for (int i = 0; i < 9; i++)
//        {
//            ItemStack current = inventory.get(i);
//            ItemStack remainingStack = remaining.get(i);
//            if (!current.isEmpty())
//            {
//                current.decrement(1);
//            }
//            if (!remainingStack.isEmpty())
//            {
//                if (current.isEmpty())
//                {
//                    inventory.set(i, remainingStack);
//                }
//                else if (ItemStack.areItemsEqualIgnoreDamage(current, remainingStack) && ItemStack.areTagsEqual(current, remainingStack))
//                {
//                    current.increment(remainingStack.getCount());
//                }
//                else
//                {
//                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainingStack);
//                }
//            }
//        }
//        markDirty();
//        return result;
//    }

    public ItemStack craft()
    {
        if (this.world == null) return ItemStack.EMPTY;
        Optional<IRecipe> optionalRecipe = getCurrentRecipe();
        if (!optionalRecipe.isPresent()) return ItemStack.EMPTY;
        IRecipe recipe = optionalRecipe.get();
        ItemStack stack = recipe.getCraftingResult(this.inventory);
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(this.inventory);

        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack itemstack = this.inventory.getStackInSlot(i);
            ItemStack itemstack1 = remaining.get(i);

            if (!itemstack.isEmpty())
            {
                this.decrStackSize(i + 1, 1);
                itemstack = this.inventory.getStackInSlot(i);
            }

            if (!itemstack1.isEmpty())
            {
                if (itemstack.isEmpty())
                {
                    this.setInventorySlotContents(i + 1, itemstack1);
                }
                else if (ItemStack.areItemsEqual(itemstack, stack) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
                {
                    itemstack1.grow(itemstack.getCount());
                    this.setInventorySlotContents(i + 1, itemstack1);
                }else{
                    // drop item
                    // this.player.dropItem(itemstack1, false);
                }
//                else if (!this.player.inventory.addItemStackToInventory(itemstack1))
//                {
//                    this.player.dropItem(itemstack1, false);
//                }
            }
        }
        return stack;
    }

    public void onContainerClose(AutoCraftingTableContainer container)
    {
        this.openContainers.remove(container);
    }
}
