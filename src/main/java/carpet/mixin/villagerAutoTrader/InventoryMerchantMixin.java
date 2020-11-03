package carpet.mixin.villagerAutoTrader;

import carpet.utils.extensions.ExtendedEntityVillagerAutotrader;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.village.MerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMerchant.class)
public class InventoryMerchantMixin {
    @Shadow @Final private IMerchant merchant;
    @Shadow private MerchantRecipe currentRecipe;

    @Inject(method = "resetRecipeAndSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryMerchant;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void addToFirstList(CallbackInfo ci) {
        if (merchant instanceof EntityVillager) {
            ((ExtendedEntityVillagerAutotrader) merchant).addToFirstList(currentRecipe);
        }
    }
}
