package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ServerRecipeBookHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerRecipeBookHelper.class)
public class ServerRecipeBookHelperMixin {
    @Shadow private InventoryCrafting field_194336_g;
    @Shadow private InventoryCraftResult field_194335_f;

    // Intentional duping bug added back for compatibility with 12.0, community request. CARPET-XCOM
    @Inject(method = "func_194327_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ServerRecipeBookHelper;func_194329_b()V", shift = At.Shift.AFTER))
    private void craftingWindowDupingBugAddedBack(EntityPlayerMP player, IRecipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci){
        if (!CarpetSettings.craftingWindowDuplication) return;
        int slot = ((DupingPlayer) player).getDupeItem();
        if(slot == Integer.MIN_VALUE) return;
        ItemStack dupeItem = player.inventory.getStackInSlot(slot);
        if(dupeItem.isEmpty()) return;

        int size = dupeItem.getCount();

        for (int j = 0; j < this.field_194336_g.getSizeInventory(); ++j)
        {
            ItemStack itemstack = this.field_194336_g.getStackInSlot(j);

            if (!itemstack.isEmpty())
            {
                size += itemstack.getCount();
                itemstack.setCount(0);
            }
        }

        ((ExtendedItemStack) (Object) dupeItem).forceStackSize(size);
        field_194335_f.clear();
        ((DupingPlayer) player).clearDupeItem();
    }
}
