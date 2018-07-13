package carpet.helpers;
//Author: xcom

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class FireworksCraftingRecipes
{

    public static IRecipe rocketOne;
    public static IRecipe rocketTwo;
    public static IRecipe rocketThree;
    
	public static void createRockets() {
		ItemStack[] resultItem = new ItemStack[3];
		
		Ingredient[] ingr1 = {Ingredient.func_193369_a(new ItemStack(Items.PAPER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)) };
	    Ingredient[] ingr2 = {Ingredient.func_193369_a(new ItemStack(Items.PAPER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)) };
	    Ingredient[] ingr3 = {Ingredient.func_193369_a(new ItemStack(Items.PAPER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.func_193369_a(new ItemStack(Items.GUNPOWDER, 1)) };
	    
        for(int i = 0; i < 3; i++){
        	resultItem[i] = new ItemStack(Items.FIREWORKS, 3);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Flight", (byte)(i+1));
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("Fireworks", nbttagcompound1);
            resultItem[i].setTagCompound(nbttagcompound3);
        }
        
        rocketOne = new ShapelessRecipes("rocket", resultItem[0], createRecipeList(ingr1));
        rocketTwo = new ShapelessRecipes("rocket", resultItem[1], createRecipeList(ingr2));
        rocketThree = new ShapelessRecipes("rocket", resultItem[2], createRecipeList(ingr3));
	}
	
    private static NonNullList<Ingredient> createRecipeList(Ingredient[] p_193364_0_)
    {
        NonNullList<Ingredient> nonnulllist = NonNullList.<Ingredient>func_191196_a();

        for (int i = 0; i < p_193364_0_.length; ++i)
        {
            Ingredient ingredient = p_193364_0_[i];

            if (ingredient != Ingredient.field_193370_a)
            {
                nonnulllist.add(ingredient);
            }
        }

        return nonnulllist;
    }
}
