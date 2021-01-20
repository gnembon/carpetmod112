package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireChargeItem.class)
public class FireChargeItemMixin extends Item {
    @Override
    public TypedActionResult<ItemStack> use(World itemStackIn, PlayerEntity worldIn, Hand playerIn) {
        if (!(CarpetSettings.rideableGhasts && worldIn.getVehicle() instanceof GhastEntity)) {
            return super.use(itemStackIn, worldIn, playerIn);
        }
        ItemStack itemstack = worldIn.getStackInHand(playerIn);
        GhastEntity ghast = (GhastEntity) worldIn.getVehicle();
        worldIn.getItemCooldownManager().set(this, 40);
        GhastHelper.set_off_fball(ghast, itemStackIn, worldIn);
        if (!worldIn.abilities.creativeMode) {
            itemstack.decrement(1);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
    }
}
