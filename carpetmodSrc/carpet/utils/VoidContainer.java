package carpet.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public class VoidContainer extends Container
{
    public VoidContainer()
    {
        super();
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return false;
    }
    
    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
    
    }
}