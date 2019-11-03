package carpet.helpers;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

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
    public NonNullList<ItemStack> inventory;
    public ItemStack output = ItemStack.EMPTY;
    private List<AutoCraftingTableContainer> openContainers = new ArrayList<>();

    /*
    public CraftingTableBlockEntity() {  //this(BlockEntityType.BARREL);
        this(TYPE);
    }
    */

    private InventoryCrafting craftMatrix = new InventoryCrafting(null, 3, 3);

    public CraftingTableBlockEntity()
    {
        super();
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY);
//        craftingInventory.stackList = this.inventory;
        craftMatrix.clear();
    }

    public static void init()
    {
    } // registers BE type

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, inventory);
        tag.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        ItemStackHelper.loadAllItems(tag, inventory);
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
    public String getGuiID() {
        return null;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing dir)
    {
        if (dir == EnumFacing.DOWN && (!output.isEmpty() /*|| getCurrentRecipe().isPresent()*/))
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
            return !output.isEmpty() /*|| getCurrentRecipe().isPresent()*/;
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
        for (ItemStack stack : this.inventory)
        {
            if (!stack.isEmpty())
                return false;
        }
        return output.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (slot > 0)
            return this.inventory.get(slot - 1);
        if (!output.isEmpty())
            return output;
//        Optional<CraftingRecipe> recipe = getCurrentRecipe();
//        return recipe.map(craftingRecipe -> craftingRecipe.craft(craftingInventory)).orElse(ItemStack.EMPTY);
        return CraftingManager.getRemainingItems(this.craftMatrix, world).get(0); // TODO: this is wrong
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
        return ItemStackHelper.getAndSplit(this.inventory, slot - 1, amount);
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
        return ItemStackHelper.getAndRemove(this.inventory, slot - 1);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            output = stack;
            return;
        }
        inventory.set(slot - 1, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 3;
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

//    private Optional<CraftingRecipe> getCurrentRecipe()
//    {
//        if (this.world == null)
//            return Optional.empty();
//        return this.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
//    }

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
        ItemStack stack = null;
        NonNullList<ItemStack> nonnulllist = CraftingManager.getRemainingItems(this.craftMatrix, world);

        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
            stack = nonnulllist.get(i);

            if (!itemstack.isEmpty())
            {
                this.craftMatrix.decrStackSize(i, 1);
                itemstack = this.craftMatrix.getStackInSlot(i);
            }

            if (!stack.isEmpty())
            {
                if (itemstack.isEmpty())
                {
                    this.craftMatrix.setInventorySlotContents(i, stack);
                }
                else if (ItemStack.areItemsEqual(itemstack, stack) && ItemStack.areItemStackTagsEqual(itemstack, stack))
                {
                    stack.grow(itemstack.getCount());
                    this.craftMatrix.setInventorySlotContents(i, stack);
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
