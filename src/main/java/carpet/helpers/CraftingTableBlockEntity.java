package carpet.helpers;


import carpet.mixin.accessors.CraftingInventoryAccessor;
import carpet.mixin.accessors.HopperBlockEntityAccessor;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
public class CraftingTableBlockEntity extends AbstractParentElement implements SidedInventory
{
    private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public CraftingInventory inventory = new CraftingInventory(null, 3, 3);
    public ItemStack output = ItemStack.EMPTY;
    private List<ContainerAutoCraftingTable> openContainers = new ArrayList<>();
    private int amountCrafted = 0;
    private PlayerEntity player;

    /*
    public TileEntityCraftingTable() {  //this(BlockEntityType.BARREL);
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

    public static boolean checkIfCanCraft(Inventory source, Inventory destination, ItemStack itemstack) {
        if (destination instanceof CraftingTableBlockEntity && !itemstack.isEmpty()) {
            int i = source.getInvSize();
            for (int j = 0; j < i; ++j) {
                if(source.getInvStack(j).isEmpty()){
                    return false;
                }
                if(HopperBlockEntityAccessor.invokeCanMergeItems(itemstack, source.getInvStack(j))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag)
    {
        super.toTag(tag);
        Inventories.toTag(tag, ((CraftingInventoryAccessor) inventory).getStacks());
        tag.put("Output", output.toTag(new CompoundTag()));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag)
    {
        super.fromTag(tag);
        Inventories.fromTag(tag, ((CraftingInventoryAccessor) inventory).getStacks());
        this.output = new ItemStack(tag.getCompound("Output"));
    }

    @Override
    public String method_29611() {
        return null;
    }

    @Override
    public boolean method_34200() {
        return false;
    }

    @Override
    public Text getDisplayName()
    {
        return new TranslatableText("container.crafting");
    }

    // Not sure about this one so left it commented
    @Override
    public Container method_34189(PlayerInventory playerInventory, PlayerEntity playerIn)
    {
        ContainerAutoCraftingTable container = new ContainerAutoCraftingTable(playerInventory, this, this.world, this.pos);
        ((CraftingInventoryAccessor) inventory).setContainer(container);
        this.openContainers.add(container);
        return container;
    }

    @Override
    public String method_34190()
    {
        return "minecraft:crafting_table";
    }

    @Override
    public int[] getInvAvailableSlots(Direction dir)
    {
        if (dir == Direction.DOWN && (!output.isEmpty() || getCurrentRecipe().isPresent()))
            return OUTPUT_SLOTS;
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir)
    {
        return slot > 0 && getInvStack(slot).isEmpty();
    }

    @Override
    public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir)
    {
        if (slot == 0)
            return !output.isEmpty() || getCurrentRecipe().isPresent();
        return true;
    }

    @Override
    public boolean isValidInvStack(int slot, ItemStack stack)
    {
        return slot != 0;
    }

    @Override
    public int method_34164(int id) {
        return 0;
    }

    @Override
    public void method_34161(int id, int value) {

    }

    @Override
    public int method_34167() {
        return 0;
    }

    @Override
    public int getInvSize()
    {
        return 10;
    }

    @Override
    public boolean isInvEmpty()
    {
        return inventory.isInvEmpty() && output.isEmpty();
    }

    @Override
    public ItemStack getInvStack(int slot)
    {
        if (slot > 0)
            return this.inventory.getInvStack(slot - 1);
        if (!output.isEmpty())
            return output;
        Optional<Recipe> recipe = getCurrentRecipe();
        return recipe.map(r -> r.method_25712(inventory)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack takeInvStack(int slot, int amount)
    {
        if (slot == 0)
        {
            if (output.isEmpty())
            {
                amountCrafted += amount;
                output = craft(this.player);
            }
            return output.split(amount);
        }
        return Inventories.splitStack(((CraftingInventoryAccessor) inventory).getStacks(), slot - 1, amount);
    }

    @Override
    public ItemStack removeInvStack(int slot)
    {
        if (slot == 0)
        {
            Thread.dumpStack();
            ItemStack output = this.output;
            this.output = ItemStack.EMPTY;
            return output;
        }
        return this.inventory.removeInvStack(slot - 1);
    }

    @Override
    public void setInvStack(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            output = stack;
            return;
        }
        ((CraftingInventoryAccessor) inventory).getStacks().set(slot - 1, stack);
    }

    @Override
    public int getInvMaxStackAmount() {
        return 64;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        for (ContainerAutoCraftingTable c : openContainers){
            c.onContentChanged(this);
        }
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity var1)
    {
        return true;
    }

    @Override
    public void onInvOpen(PlayerEntity player) {

    }

    @Override
    public void onInvClose(PlayerEntity player) {

    }

    @Override
    public void clear()
    {
        this.inventory.clear();
    }

    private Optional<Recipe> getCurrentRecipe()
    {
        if (this.world == null)
            return Optional.empty();
        return Optional.ofNullable(RecipeManager.method_25728(inventory, this.world));
    }

    protected void onCrafting(PlayerEntity player, Recipe irecipe, ItemStack stack)
    {
        if(player == null){
            return;
        }
        if (this.amountCrafted > 0)
        {
            stack.onCraft(player.world, player, this.amountCrafted);
        }

        this.amountCrafted = 0;

        if (irecipe != null && !irecipe.isIgnoredInRecipeBook())
        {
            player.method_25007(Lists.newArrayList(irecipe));
        }
    }

    public ItemStack craft(PlayerEntity player)
    {
        if (this.world == null) return ItemStack.EMPTY;
        Optional<Recipe> optionalRecipe = getCurrentRecipe();
        if (!optionalRecipe.isPresent()) return ItemStack.EMPTY;
        Recipe recipe = optionalRecipe.get();
        ItemStack stack = recipe.method_25712(this.inventory);
        onCrafting(player, recipe, stack);
        DefaultedList<ItemStack> remaining = recipe.method_25715(this.inventory);

        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack itemstack = this.inventory.getInvStack(i);
            ItemStack itemstack1 = remaining.get(i);

            if (!itemstack.isEmpty())
            {
                this.takeInvStack(i + 1, 1);
                itemstack = this.inventory.getInvStack(i);
            }

            if (!itemstack1.isEmpty())
            {
                if (itemstack.isEmpty())
                {
                    this.setInvStack(i + 1, itemstack1);
                }
                else if (ItemStack.areItemsEqualIgnoreDamage(itemstack, stack) && ItemStack.areTagsEqual(itemstack, itemstack1))
                {
                    itemstack1.increment(itemstack.getCount());
                    this.setInvStack(i + 1, itemstack1);
                }else{
                    ItemScatterer.method_34178(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemstack1);
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
        ItemScatterer.spawn(worldIn, pos, inventory);
        ItemScatterer.method_34178(worldIn, pos.getX(), pos.getY(), pos.getZ(), output);
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }
}