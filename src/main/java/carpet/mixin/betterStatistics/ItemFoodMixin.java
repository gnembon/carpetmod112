package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFood.class)
public class ItemFoodMixin {
    @Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getObjectUseStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addUseMeta(Item item, ItemStack stack) {
        return StatHelper.getObjectUseStats(item, stack.getMetadata());
    }
}
