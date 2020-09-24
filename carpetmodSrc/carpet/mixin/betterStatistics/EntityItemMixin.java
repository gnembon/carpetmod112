package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin {
    @Shadow public abstract ItemStack getItem();

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getObjectsPickedUpStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addObjectMeta(Item item) {
        return StatHelper.getObjectsPickedUpStats(item, getItem().getItemDamage());
    }
}
