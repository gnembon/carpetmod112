package carpet.helpers;

import carpet.CarpetServer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AutoCraftingDropperHelper
{
    public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        while (!stack.isEmpty())
        {
            EntityItem entityitem = new EntityItem(worldIn, x, y, z, stack.splitStack(CarpetServer.rand.nextInt(21) + 10));
            entityitem.motionX = (CarpetServer.rand.nextDouble() - CarpetServer.rand.nextDouble()) * 0.05;
            entityitem.motionY = CarpetServer.rand.nextDouble() * 0.05;
            entityitem.motionZ = (CarpetServer.rand.nextDouble() - CarpetServer.rand.nextDouble()) * 0.05;
            worldIn.spawnEntity(entityitem);
        }
    }
}
