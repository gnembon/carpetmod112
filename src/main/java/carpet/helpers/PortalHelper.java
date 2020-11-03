package carpet.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;

public class PortalHelper
{
    public static boolean player_holds_obsidian(EntityPlayer playerIn)
    {
        if ( (!playerIn.getHeldItemMainhand().isEmpty()
            && playerIn.getHeldItemMainhand().getItem() instanceof ItemBlock &&
            ((ItemBlock)(playerIn.getHeldItemMainhand().getItem())).getBlock() == Blocks.OBSIDIAN   ))
        {
            return true;
        }
        return false;
    }
}
