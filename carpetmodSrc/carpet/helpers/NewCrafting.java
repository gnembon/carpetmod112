package carpet.helpers;

import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.List;

public class NewCrafting implements IRecipe {

    private final ItemStack recipeOutput;
    private final NonNullList<Ingredient> recipeItems;
    private final String field_194138_c;

    public NewCrafting(String p_i47500_1_, ItemStack p_i47500_2_, NonNullList<Ingredient> p_i47500_3_) {
        this.field_194138_c = p_i47500_1_;
        this.recipeOutput = p_i47500_2_;
        this.recipeItems = p_i47500_3_;
    }
    
    public static void registerRecipes() {
        CraftingManager.register("rocket1", rocketOne);
        CraftingManager.register("rocket2", rocketTwo);
        CraftingManager.register("rocket3", rocketThree);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        List<Ingredient> list = Lists.newArrayList(this.recipeItems);

        for (int i = 0; i < inv.getHeight(); ++i) {
            for (int j = 0; j < inv.getWidth(); ++j) {
                ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

                if (!itemstack.isEmpty()) {
                    boolean flag = false;

                    for (Ingredient ingredient : list) {
                        if (ingredient.apply(itemstack)) {
                            flag = true;
                            list.remove(ingredient);
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }
        }

        return list.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return this.recipeOutput.copy();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return this.recipeOutput;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(),
                ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (itemstack.getItem().hasContainerItem()) {
                nonnulllist.set(i, new ItemStack(itemstack.getItem().getContainerItem()));
            }
        }

        return nonnulllist;
    }

    public boolean func_192399_d() {
        return false;
    }

    private static NonNullList<Ingredient> func_193364_a(Ingredient[] p_193364_0_) {
        NonNullList<Ingredient> nonnulllist = NonNullList.<Ingredient>create();

        for (int i = 0; i < p_193364_0_.length; ++i) {
            Ingredient ingredient = p_193364_0_[i];

            if (ingredient != Ingredient.EMPTY) {
                nonnulllist.add(ingredient);
            }
        }

        return nonnulllist;
    }

    public static IRecipe rocketOne;
    public static IRecipe rocketTwo;
    public static IRecipe rocketThree;

    public static void createRockets() {
        ItemStack[] resultItem = new ItemStack[3];
        IRecipe[] rocket = new IRecipe[3];

        Ingredient[] ingr1 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};
        Ingredient[] ingr2 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};
        Ingredient[] ingr3 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};

        for (int i = 0; i < 3; i++) {
            resultItem[i] = new ItemStack(Items.FIREWORKS, 3);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Flight", (byte) (i + 1));
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("Fireworks", nbttagcompound1);
            resultItem[i].setTagCompound(nbttagcompound3);
        }

        rocketOne = new ShapelessRecipes("rocket", resultItem[0], func_193364_a(ingr1));
        rocketTwo = new ShapelessRecipes("rocket", resultItem[1], func_193364_a(ingr2));
        rocketThree = new ShapelessRecipes("rocket", resultItem[2], func_193364_a(ingr3));
    }
}

