package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow private int itemDamage;

    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getObjectUseStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addUseMeta1(Item item) {
        return StatHelper.getObjectUseStats(item, itemDamage);
    }

    @Redirect(method = "hitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getObjectUseStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addUseMeta2(Item item) {
        return StatHelper.getObjectUseStats(item, itemDamage);
    }

    @Redirect(method = "onBlockDestroyed", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getObjectUseStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addUseMeta3(Item item) {
        return StatHelper.getObjectUseStats(item, itemDamage);
    }

    @Redirect(method = "onCrafting", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getCraftStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addCraftMeta(Item item) {
        return StatHelper.getCraftStats(item, itemDamage);
    }
}
