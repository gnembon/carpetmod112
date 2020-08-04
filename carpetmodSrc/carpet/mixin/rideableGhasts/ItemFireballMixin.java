package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFireball.class)
public class ItemFireballMixin extends Item {
    @Override
    public ActionResult<ItemStack> onItemRightClick(World itemStackIn, EntityPlayer worldIn, EnumHand playerIn) {
        if (!(CarpetSettings.rideableGhasts && worldIn.getRidingEntity() instanceof EntityGhast)) {
            return super.onItemRightClick(itemStackIn, worldIn, playerIn);
        }
        ItemStack itemstack = worldIn.getHeldItem(playerIn);
        EntityGhast ghast = (EntityGhast) worldIn.getRidingEntity();
        worldIn.getCooldownTracker().setCooldown(this, 40);
        GhastHelper.set_off_fball(ghast, itemStackIn, worldIn);
        if (!worldIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}
