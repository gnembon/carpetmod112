package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    @Redirect(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33897(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addObjectMeta(Item item) {
        return StatHelper.getObjectsPickedUpStats(item, getStack().getDamage());
    }
}
