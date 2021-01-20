package carpet.helpers;

import carpet.CarpetServer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AutoCraftingDropperHelper
{
    public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        while (!stack.isEmpty())
        {
            ItemEntity itemEntity = new ItemEntity(worldIn, x, y, z, stack.split(CarpetServer.rand.nextInt(21) + 10));
            itemEntity.field_33074 = (CarpetServer.rand.nextDouble() - CarpetServer.rand.nextDouble()) * 0.05;
            itemEntity.field_33075 = CarpetServer.rand.nextDouble() * 0.05;
            itemEntity.field_33076 = (CarpetServer.rand.nextDouble() - CarpetServer.rand.nextDouble()) * 0.05;
            worldIn.method_26040(itemEntity);
        }
    }
}
