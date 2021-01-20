package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow private int damage;

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33892(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addUseMeta1(Item item) {
        return StatHelper.getObjectUseStats(item, damage);
    }

    @Redirect(method = "postHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33892(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addUseMeta2(Item item) {
        return StatHelper.getObjectUseStats(item, damage);
    }

    @Redirect(method = "postMine", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33892(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addUseMeta3(Item item) {
        return StatHelper.getObjectUseStats(item, damage);
    }

    @Redirect(method = "onCraft", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33885(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addCraftMeta(Item item) {
        return StatHelper.getCraftStats(item, damage);
    }
}
