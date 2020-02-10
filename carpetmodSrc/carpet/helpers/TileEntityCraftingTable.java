package carpet.helpers;


import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 *
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile
 * crafting table turns into a automatic crafting table where it can be used to automatically craft items.
 */
public class TileEntityCraftingTable extends TileEntityLockable implements ISidedInventory
{
    private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public InventoryCrafting inventory = new InventoryCrafting(null, 3, 3);
    public ItemStack output = ItemStack.EMPTY;
    private List<ContainerAutoCraftingTable> openContainers = new ArrayList<>();
    private int amountCrafted = 0;
    private EntityPlayer player;

    /*
    public TileEntityCraftingTable() {  //this(BlockEntityType.BARREL);
        this(TYPE);
    }
    */

    public TileEntityCraftingTable()
    {
        super();
    }

    public static void init()
    {
    } // registers BE type

    public static boolean checkIfCanCraft(IInventory source, IInventory destination, ItemStack itemstack) {
        if (destination instanceof TileEntityCraftingTable && !itemstack.isEmpty()) {
            int i = source.getSizeInventory();
            for (int j = 0; j < i; ++j) {
                if(source.getStackInSlot(j).isEmpty()){
                    return false;
                }
                if(TileEntityHopper.canCombine(itemstack, source.getStackInSlot(j))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

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

    // Not sure about this one so left it commented
    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        ContainerAutoCraftingTable container = new ContainerAutoCraftingTable(playerInventory, this, this.world, this.pos);
        inventory.eventHandler = container;
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
                amountCrafted += amount;
                output = craft(this.player);
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
            Thread.dumpStack();
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
        for (ContainerAutoCraftingTable c : openContainers){
            c.onCraftMatrixChanged(this);
        }
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

    protected void onCrafting(EntityPlayer player, IRecipe irecipe, ItemStack stack)
    {
        if(player == null){
            return;
        }
        if (this.amountCrafted > 0)
        {
            stack.onCrafting(player.world, player, this.amountCrafted);
        }

        this.amountCrafted = 0;

        if (irecipe != null && !irecipe.isDynamic())
        {
            player.unlockRecipes(Lists.newArrayList(irecipe));
        }
    }

    public ItemStack craft(EntityPlayer player)
    {
        if (this.world == null) return ItemStack.EMPTY;
        Optional<IRecipe> optionalRecipe = getCurrentRecipe();
        if (!optionalRecipe.isPresent()) return ItemStack.EMPTY;
        IRecipe recipe = optionalRecipe.get();
        ItemStack stack = recipe.getCraftingResult(this.inventory);
        onCrafting(player, recipe, stack);
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
                    InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemstack1);
                }
            }
        }
        return stack;
    }

    public void onContainerClose(ContainerAutoCraftingTable container)
    {
        this.openContainers.remove(container);
    }

    public void dropContent(World worldIn, BlockPos pos){
        InventoryHelper.dropInventoryItems(worldIn, pos, inventory);
        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), output);
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }
}