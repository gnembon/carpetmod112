package carpet.utils;

import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;

public class VoidContainer extends Container
{
    public VoidContainer()
    {
        super();
    }
    
    @Override
    public boolean canUse(PlayerEntity player)
    {
        return false;
    }
    
    @Override
    public void onContentChanged(Inventory inv)
    {
    
    }
}