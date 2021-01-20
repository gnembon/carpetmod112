package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.class_4522;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4522.class)
public class ServerRecipeBookHelperMixin {
    @Shadow private CraftingInventory field_31525;
    @Shadow private CraftingResultInventory field_31524;

    // Intentional duping bug added back for compatibility with 12.0, community request. CARPET-XCOM
    @Inject(method = "method_33169", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4522;method_33170()V", shift = At.Shift.AFTER))
    private void craftingWindowDupingBugAddedBack(ServerPlayerEntity player, Recipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci){
        if (!CarpetSettings.craftingWindowDuplication) return;
        int slot = ((DupingPlayer) player).getDupeItem();
        if(slot == Integer.MIN_VALUE) return;
        ItemStack dupeItem = player.inventory.getInvStack(slot);
        if(dupeItem.isEmpty()) return;

        int size = dupeItem.getCount();

        for (int j = 0; j < this.field_31525.getInvSize(); ++j)
        {
            ItemStack itemstack = this.field_31525.getInvStack(j);

            if (!itemstack.isEmpty())
            {
                size += itemstack.getCount();
                itemstack.setCount(0);
            }
        }

        ((ExtendedItemStack) (Object) dupeItem).forceStackSize(size);
        field_31524.clear();
        ((DupingPlayer) player).clearDupeItem();
    }
}
