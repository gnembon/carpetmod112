package carpet.mixin.villagerAutoTrader;

import carpet.utils.extensions.AutotraderVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.Trader;
import net.minecraft.village.TraderInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TraderInventory.class)
public class TraderInventoryMixin {
    @Shadow @Final private Trader trader;
    @Shadow private TradeOffer traderRecipe;

    @Inject(method = "updateRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TraderInventory;setInvStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void addToFirstList(CallbackInfo ci) {
        if (trader instanceof VillagerEntity) {
            ((AutotraderVillagerEntity) trader).addToFirstList(traderRecipe);
        }
    }
}
